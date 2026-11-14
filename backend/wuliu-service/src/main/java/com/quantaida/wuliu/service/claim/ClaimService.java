package com.quantaida.wuliu.service.claim;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.wuliu.common.context.TenantContext;
import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.domain.entity.*;
import com.quantaida.wuliu.domain.mapper.*;
import com.quantaida.wuliu.service.message.MessageService;
import com.quantaida.wuliu.service.system.AuditService;
import com.quantaida.wuliu.service.system.NumberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 理赔：立案（证据必填）→ 定责 → 审批 → 赔付（冲减毛利）→ 结案。 */
@Service
@RequiredArgsConstructor
public class ClaimService {
    private final ClaimMapper claimMapper;
    private final WaybillMapper waybillMapper;
    private final ComplaintMapper complaintMapper;
    private final NumberService numberService;
    private final MessageService messageService;
    private final AuditService auditService;

    @Transactional
    public Claim create(Long waybillId, String type, String description, List<String> evidence) {
        Waybill wb = waybillMapper.selectById(waybillId);
        if (wb == null) throw BizException.notFound("运单不存在");
        if (type == null || type.isBlank()) throw new BizException("请选择理赔类型");
        if (evidence == null || evidence.isEmpty()) throw new BizException("请上传照片或视频作为证据，否则不能立案");
        Claim claim = new Claim();
        claim.setClaimNo(numberService.next("LP", TenantContext.requireTenantId()));
        claim.setWaybillId(waybillId);
        claim.setWaybillNo(wb.getWaybillNo());
        claim.setRecordType(type);
        claim.setDescription(description);
        try {
            claim.setEvidence(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(evidence));
        } catch (Exception ignored) {
        }
        claim.setStatus("已立案");
        claim.setHandler(TenantContext.username());
        claimMapper.insert(claim);
        messageService.notify("理赔进度", waybillId, wb.getSenderPhone(), wb.getWaybillNo(), "已立案");
        auditService.log("理赔立案", claim.getClaimNo(), type + "：" + description);
        return claim;
    }

    @Transactional
    public void liability(Long id, String liability, String reason) {
        Claim claim = require(id);
        if (!List.of("已立案", "定责中").contains(claim.getStatus())) {
            throw new BizException("理赔单当前状态为「" + claim.getStatus() + "」，不能定责");
        }
        claim.setLiability(liability);
        claim.setLiabilityReason(reason);
        claim.setStatus("待审批");
        claimMapper.updateById(claim);
        auditService.log("理赔定责", claim.getClaimNo(), liability + "：" + reason);
    }

    @Transactional
    public void approve(Long id, boolean pass, BigDecimal amount, String reason) {
        Claim claim = require(id);
        if (!"待审批".equals(claim.getStatus())) throw new BizException("理赔单不在待审批状态");
        if (pass) {
            if (amount == null || amount.signum() <= 0) throw new BizException("请填写赔付金额");
            claim.setAmount(amount);
            claim.setStatus("已赔付");
            Waybill wb = waybillMapper.selectById(claim.getWaybillId());
            if (wb != null) {
                wb.setGrossProfit((wb.getGrossProfit() == null ? BigDecimal.ZERO : wb.getGrossProfit()).subtract(amount));
                waybillMapper.updateById(wb);
                messageService.notify("理赔进度", wb.getId(), wb.getSenderPhone(), wb.getWaybillNo(), "已赔付 " + amount + " 元");
            }
        } else {
            claim.setStatus("驳回");
            claim.setRejectReason(reason);
        }
        claimMapper.updateById(claim);
        auditService.log("理赔审批", claim.getClaimNo(), pass ? "通过 " + amount : "驳回：" + reason);
    }

    @Transactional
    public void close(Long id) {
        Claim claim = require(id);
        if (!"已赔付".equals(claim.getStatus())) throw new BizException("只有已赔付的理赔单可以结案");
        claim.setStatus("已结案");
        claim.setClosedAt(LocalDateTime.now());
        claimMapper.updateById(claim);
        auditService.log("理赔结案", claim.getClaimNo(), "");
    }

    public List<Claim> list() {
        return claimMapper.selectList(new LambdaQueryWrapper<Claim>().orderByDesc(Claim::getId));
    }

    public List<Complaint> complaints() {
        return complaintMapper.selectList(new LambdaQueryWrapper<Complaint>().orderByDesc(Complaint::getId));
    }

    public Complaint createComplaint(Complaint complaint) {
        complaint.setId(null);
        if (complaint.getStatus() == null) complaint.setStatus("待处理");
        complaintMapper.insert(complaint);
        return complaint;
    }

    private Claim require(Long id) {
        Claim claim = claimMapper.selectById(id);
        if (claim == null) throw BizException.notFound("理赔单不存在");
        return claim;
    }
}
