package com.quantaida.wuliu.service.sign;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.wuliu.common.context.TenantContext;
import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.domain.entity.*;
import com.quantaida.wuliu.domain.mapper.*;
import com.quantaida.wuliu.service.message.MessageService;
import com.quantaida.wuliu.service.order.WaybillService;
import com.quantaida.wuliu.service.system.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 签收、电子回单、拒收退回。 */
@Service
@RequiredArgsConstructor
public class SignService {
    private final WaybillMapper waybillMapper;
    private final SignRecordMapper signMapper;
    private final ReceiptMapper receiptMapper;
    private final ReturnTaskMapper returnMapper;
    private final WaybillService waybillService;
    private final MessageService messageService;
    private final AuditService auditService;

    @Transactional
    public SignRecord sign(Long waybillId, String signerName, String signerPhone, String signImage,
                           BigDecimal lat, BigDecimal lng) {
        Waybill wb = require(waybillId);
        if (!"派送中".equals(wb.getStatus())) {
            throw new BizException("运单当前状态为「" + wb.getStatus() + "」，只有派送中才能签收");
        }
        if ((signerName == null || signerName.isBlank())) throw new BizException("请填写签收人姓名");
        if ((signImage == null || signImage.isBlank())) throw new BizException("请收货人签名");
        SignRecord record = new SignRecord();
        record.setWaybillId(waybillId);
        record.setSignerName(signerName);
        record.setSignerPhone(signerPhone);
        record.setSignImage(signImage);
        record.setLat(lat);
        record.setLng(lng);
        record.setSignedAt(LocalDateTime.now());
        signMapper.insert(record);
        waybillService.transition(waybillId, "sign", "收货人签收", null);
        auditService.log("签收", wb.getWaybillNo(), "签收人：" + signerName);
        return record;
    }

    @Transactional
    public Receipt uploadReceipt(Long waybillId, List<String> images) {
        Waybill wb = require(waybillId);
        if (!List.of("已签收", "待对账", "已对账待开票", "已结算").contains(wb.getStatus())) {
            throw new BizException("运单尚未签收，不能上传回单");
        }
        if (images == null || images.isEmpty()) throw new BizException("请上传至少一张回单照片");
        Receipt receipt = receiptMapper.selectOne(new LambdaQueryWrapper<Receipt>()
                .eq(Receipt::getWaybillId, waybillId).orderByDesc(Receipt::getId).last("limit 1"));
        if (receipt == null) {
            receipt = new Receipt();
            receipt.setWaybillId(waybillId);
        }
        try {
            receipt.setImages(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(images));
        } catch (Exception ignored) {
        }
        receipt.setStatus("待审核");
        receipt.setRejectReason("");
        receiptMapper.insertOrUpdate(receipt);
        auditService.log("上传回单", wb.getWaybillNo(), "共 " + images.size() + " 张");
        return receipt;
    }

    @Transactional
    public void reviewReceipt(Long id, boolean pass, String reason) {
        Receipt receipt = receiptMapper.selectById(id);
        if (receipt == null) throw BizException.notFound("回单不存在");
        receipt.setStatus(pass ? "已通过" : "已驳回");
        receipt.setRejectReason(pass ? "" : reason);
        receipt.setReviewer(TenantContext.username());
        receipt.setReviewedAt(LocalDateTime.now());
        receiptMapper.updateById(receipt);
        Waybill wb = waybillMapper.selectById(receipt.getWaybillId());
        if (pass && wb != null && "已签收".equals(wb.getStatus())) {
            waybillService.transition(wb.getId(), "receipt", "回单审核通过", null);
            messageService.notify("回单完成", wb.getId(), wb.getSenderPhone(), wb.getWaybillNo(), "回单已审核通过");
        }
        if (wb != null) auditService.log("回单审核", wb.getWaybillNo(), pass ? "通过" : "驳回：" + reason);
    }

    /** 拒收：生成退回任务，退回运费按逆向规则（基础运费 50%，最低 50 元）。 */
    @Transactional
    public ReturnTask reject(Long waybillId, String reason, String disposition, List<String> images) {
        Waybill wb = require(waybillId);
        if (!"派送中".equals(wb.getStatus())) throw new BizException("只有派送中的运单可以拒收");
        if (reason == null || reason.isBlank()) throw new BizException("请选择拒收原因");
        if (images == null || images.isEmpty()) throw new BizException("请上传现场照片");
        waybillService.transition(waybillId, "reject", "收货人拒收：" + reason, null);
        ReturnTask task = new ReturnTask();
        task.setWaybillId(waybillId);
        task.setReason(reason);
        task.setDisposition(disposition == null ? "退回发货人" : disposition);
        BigDecimal fee = wb.getBaseFreight() == null ? BigDecimal.ZERO : wb.getBaseFreight().multiply(new BigDecimal("0.5"));
        task.setReturnFee(fee.max(new BigDecimal("50")));
        task.setStatus("退回中");
        returnMapper.insert(task);
        messageService.notify("拒收通知", waybillId, wb.getSenderPhone(), wb.getWaybillNo(), reason);
        auditService.log("拒收", wb.getWaybillNo(), reason);
        return task;
    }

    @Transactional
    public void completeReturn(Long id) {
        ReturnTask task = returnMapper.selectById(id);
        if (task == null) throw BizException.notFound("退回任务不存在");
        task.setStatus("已退回");
        task.setCompletedAt(LocalDateTime.now());
        returnMapper.updateById(task);
        Waybill wb = require(task.getWaybillId());
        try {
            waybillService.transition(wb.getId(), "returnSign", "退回签收", null);
        } catch (BizException ignored) {
        }
        auditService.log("退回签收", wb.getWaybillNo(), String.valueOf(task.getReturnFee()));
    }

    public Map<String, Object> h5Receipt(String waybillNo, String sign) {
        Waybill wb = waybillMapper.selectOne(new LambdaQueryWrapper<Waybill>()
                .eq(Waybill::getWaybillNo, waybillNo).last("limit 1"));
        if (wb == null) throw BizException.notFound("运单不存在或回单尚未上传");
        Receipt receipt = receiptMapper.selectOne(new LambdaQueryWrapper<Receipt>()
                .eq(Receipt::getWaybillId, wb.getId()).orderByDesc(Receipt::getId).last("limit 1"));
        SignRecord record = signMapper.selectOne(new LambdaQueryWrapper<SignRecord>()
                .eq(SignRecord::getWaybillId, wb.getId()).orderByDesc(SignRecord::getId).last("limit 1"));
        if (receipt == null && record == null) throw BizException.notFound("该运单暂无电子回单");
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("waybillNo", wb.getWaybillNo());
        m.put("status", wb.getStatus());
        m.put("senderName", wb.getSenderName());
        m.put("receiverName", wb.getReceiverName());
        m.put("origin", wb.getOrigin());
        m.put("destination", wb.getDestination());
        m.put("cargoName", wb.getCargoName());
        m.put("receipt", receipt);
        m.put("sign", record);
        return m;
    }

    public List<Receipt> receiptList(String status) {
        return receiptMapper.selectList(new LambdaQueryWrapper<Receipt>()
                .eq(status != null && !status.isBlank(), Receipt::getStatus, status)
                .orderByDesc(Receipt::getId));
    }

    public List<ReturnTask> returns() {
        return returnMapper.selectList(new LambdaQueryWrapper<ReturnTask>().orderByDesc(ReturnTask::getId));
    }

    private Waybill require(Long id) {
        Waybill wb = waybillMapper.selectById(id);
        if (wb == null) throw BizException.notFound("运单不存在");
        return wb;
    }
}
