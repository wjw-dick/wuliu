package com.quantaida.wuliu.service.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantaida.wuliu.common.context.TenantContext;
import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.common.util.DangerousGoodsChecker;
import com.quantaida.wuliu.domain.entity.*;
import com.quantaida.wuliu.domain.mapper.*;
import com.quantaida.wuliu.service.message.MessageService;
import com.quantaida.wuliu.service.pricing.PriceRequest;
import com.quantaida.wuliu.service.pricing.PricingService;
import com.quantaida.wuliu.service.system.AuditService;
import com.quantaida.wuliu.service.system.NumberService;
import com.quantaida.wuliu.service.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/** 运单主服务：创建、改单、状态迁移、时间轴、打印数据。 */
@Service
@RequiredArgsConstructor
public class WaybillService {
    private final WaybillMapper waybillMapper;
    private final WaybillItemMapper itemMapper;
    private final WaybillPhotoMapper photoMapper;
    private final WaybillStatusLogMapper logMapper;
    private final WaybillVersionMapper versionMapper;
    private final CustomerMapper customerMapper;
    private final SignRecordMapper signMapper;
    private final ReceiptMapper receiptMapper;
    private final TrackNodeMapper nodeMapper;
    private final DispatchMapper dispatchMapper;
    private final ReceivableMapper receivableMapper;
    private final PayableMapper payableMapper;
    private final CodSettleMapper codMapper;
    private final PricingService pricingService;
    private final WaybillStateMachine stateMachine;
    private final NumberService numberService;
    private final TenantService tenantService;
    private final MessageService messageService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @Transactional
    public Map<String, Object> create(Waybill wb, List<WaybillItem> items, List<String> photos) {
        tenantService.assertCanCreateWaybill();
        validate(wb);
        DangerousGoodsChecker.check(wb.getCargoName(), wb.getCargoType());

        Customer customer = null;
        if (wb.getCustomerId() != null) {
            customer = customerMapper.selectById(wb.getCustomerId());
            if (customer == null) throw new BizException("客户不存在");
            wb.setCustomerName(customer.getName());
            if (wb.getSettleType() == null || wb.getSettleType().isBlank()) {
                wb.setSettleType(customer.getSettleType());
            }
        }
        if (wb.getSettleType() == null || wb.getSettleType().isBlank()) wb.setSettleType("现结");
        if ("月结".equals(wb.getSettleType()) && customer != null && customer.getCreditLimit() != null) {
            BigDecimal used = nz(customer.getCreditUsed());
            BigDecimal amount = nz(wb.getTotalAmount());
            if (used.add(amount).compareTo(customer.getCreditLimit()) > 0) {
                BigDecimal remain = customer.getCreditLimit().subtract(used).max(BigDecimal.ZERO);
                throw new BizException("信用额度已用尽（剩余 " + remain + " 元），请改为现结或先回款");
            }
        }

        PriceRequest req = new PriceRequest();
        req.setCustomerId(wb.getCustomerId());
        req.setOrigin(wb.getOrigin());
        req.setDestination(wb.getDestination());
        req.setCargoType(wb.getCargoType());
        req.setCargoName(wb.getCargoName());
        req.setWeight(nz(wb.getWeight()));
        req.setVolume(nz(wb.getVolume()));
        req.setPieces(wb.getPieces() == null ? 1 : wb.getPieces());
        req.setVehicleType(wb.getVehicleType());
        req.setNeedLoading(wb.getLoadingFee() != null && wb.getLoadingFee().signum() > 0);
        req.setNeedUpstairs(wb.getUpstairsFee() != null && wb.getUpstairsFee().signum() > 0);
        req.setCodAmount(nz(wb.getCodAmount()));
        req.setCargoValue(nz(wb.getInsuranceFee()).signum() > 0 ? nz(wb.getTotalAmount()) : BigDecimal.ZERO);
        Map<String, Object> price = pricingService.quote(req, TenantContext.requireTenantId(), true);

        wb.setBaseFreight(dec(price.get("baseFreight")));
        wb.setFuelSurcharge(dec(price.get("fuelSurcharge")));
        wb.setLoadingFee(dec(price.get("loadingFee")));
        wb.setUpstairsFee(dec(price.get("upstairsFee")));
        wb.setInsuranceFee(dec(price.get("insuranceFee")));
        wb.setCodFee(dec(price.get("codFee")));
        wb.setRemoteFee(dec(price.get("remoteFee")));
        wb.setHolidayFee(dec(price.get("holidayFee")));
        wb.setTotalAmount(dec(price.get("totalAmount")));
        wb.setReceivableAmount(wb.getTotalAmount());
        wb.setPayableAmount(PricingService.money(wb.getTotalAmount().multiply(new BigDecimal("0.70"))));
        wb.setGrossProfit(PricingService.money(wb.getTotalAmount().subtract(wb.getPayableAmount())));
        wb.setRuleVersion(String.valueOf(price.get("ruleVersion")));
        wb.setQuoteNo(String.valueOf(price.get("quoteNo")));
        wb.setWaybillNo(numberService.next("YH", TenantContext.requireTenantId()));
        if (wb.getSource() == null) wb.setSource("后台代录");
        wb.setCreatedBy(TenantContext.username());
        if (wb.getInsured() == null) wb.setInsured(0);
        if (wb.getCodAmount() == null) wb.setCodAmount(BigDecimal.ZERO);

        String status = ("现结".equals(wb.getSettleType()) || "预付定金".equals(wb.getSettleType())) ? "待支付" : "已受理";
        wb.setStatus(status);
        wb.setId(null);
        waybillMapper.insert(wb);
        if (items != null) {
            for (WaybillItem item : items) {
                item.setId(null);
                item.setWaybillId(wb.getId());
                itemMapper.insert(item);
            }
        }
        if (photos != null) {
            int i = 0;
            for (String url : photos) {
                WaybillPhoto p = new WaybillPhoto();
                p.setWaybillId(wb.getId());
                p.setStage("货物");
                p.setUrl(url);
                p.setSortNo(i++);
                photoMapper.insert(p);
            }
        }
        log(wb.getId(), "", status, "create", "创建运单", null);
        messageService.notify("下单成功", wb.getId(), wb.getSenderPhone(), wb.getWaybillNo(), status);
        auditService.log("创建运单", wb.getWaybillNo(), "客户：" + wb.getCustomerName() + "，金额：" + wb.getTotalAmount());
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", wb.getId());
        result.put("waybillNo", wb.getWaybillNo());
        result.put("status", wb.getStatus());
        result.put("totalAmount", wb.getTotalAmount());
        result.put("priceDetail", price.get("detail"));
        result.put("priceSource", price.get("priceSource"));
        return result;
    }

    public void validate(Waybill wb) {
        if (blank(wb.getSenderName())) throw new BizException("请填写发货人姓名");
        if (blank(wb.getSenderPhone())) throw new BizException("请填写发货人联系电话");
        if (blank(wb.getSenderAddress())) throw new BizException("请填写发货地址");
        if (blank(wb.getReceiverName())) throw new BizException("请填写收货人姓名");
        if (blank(wb.getReceiverPhone())) throw new BizException("请填写收货人联系电话");
        if (blank(wb.getReceiverAddress())) throw new BizException("请填写收货地址");
        if (blank(wb.getCargoName())) throw new BizException("请填写货物名称，禁止空泛填写");
        if (wb.getCargoName() != null && wb.getCargoName().trim().length() < 2) {
            throw new BizException("货物名称过于笼统，请填写具体品名");
        }
        if (nz(wb.getWeight()).signum() <= 0 && nz(wb.getVolume()).signum() <= 0) {
            throw new BizException("请填写货物重量或体积");
        }
        if (blank(wb.getOrigin()) || blank(wb.getDestination())) throw new BizException("请选择出发地与目的地");
        if (wb.getPickupTime() == null) wb.setPickupTime(LocalDateTime.now().plusHours(2));
        if (wb.getPieces() == null) wb.setPieces(1);
    }

    /** 状态迁移唯一入口。 */
    @Transactional
    public Waybill transition(Long id, String action, String reason, Map<String, Object> payload) {
        Waybill wb = waybillMapper.selectById(id);
        if (wb == null) throw BizException.notFound("运单不存在");
        WaybillStateMachine.Target target = stateMachine.resolve(wb, action, payload == null ? null : str(payload.get("previousStatus")));
        String from = wb.getStatus();
        wb.setStatus(target.status());
        if ("已受理".equals(target.status())) wb.setAcceptedAt(LocalDateTime.now());
        if ("已签收".equals(target.status())) wb.setSignedAt(LocalDateTime.now());
        if ("已完结".equals(target.status())) wb.setFinishedAt(LocalDateTime.now());
        waybillMapper.updateById(wb);
        log(wb.getId(), from, target.status(), action, reason, TenantContext.username());
        afterTransition(wb, action, from, target.status(), payload);
        auditService.log("运单状态变更", wb.getWaybillNo(), from + " → " + target.status() + "（" + action + "）");
        return wb;
    }

    private void afterTransition(Waybill wb, String action, String from, String to, Map<String, Object> payload) {
        messageService.notify(nodeOf(to), wb.getId(), wb.getReceiverPhone(), wb.getWaybillNo(), to);
        if ("已签收".equals(to)) {
            createReceivable(wb);
            createPayable(wb);
            if (nz(wb.getCodAmount()).signum() > 0) {
                CodSettle cod = new CodSettle();
                cod.setWaybillId(wb.getId());
                cod.setWaybillNo(wb.getWaybillNo());
                cod.setAmount(wb.getCodAmount());
                cod.setFee(nz(wb.getCodFee()));
                cod.setStatus("待回款");
                codMapper.insert(cod);
            }
        }
        if ("已取消".equals(to)) {
            Dispatch dispatch = dispatchMapper.selectOne(new LambdaQueryWrapper<Dispatch>()
                    .eq(Dispatch::getWaybillId, wb.getId()).orderByDesc(Dispatch::getId).last("limit 1"));
            if (dispatch != null) {
                dispatch.setStatus("已取消");
                if (payload != null && payload.get("emptyFee") != null) {
                    dispatch.setEmptyFee(dec(payload.get("emptyFee")));
                }
                dispatchMapper.updateById(dispatch);
                if (dispatch.getEmptyFee() != null && dispatch.getEmptyFee().signum() > 0) {
                    messageService.notify("空驶费", wb.getId(), wb.getSenderPhone(), wb.getWaybillNo(),
                            "空驶费 " + dispatch.getEmptyFee() + " 元");
                }
            }
        }
    }

    private void createReceivable(Waybill wb) {
        Long exists = receivableMapper.selectCount(new LambdaQueryWrapper<Receivable>()
                .eq(Receivable::getWaybillId, wb.getId()));
        if (exists != null && exists > 0) return;
        Receivable r = new Receivable();
        r.setWaybillId(wb.getId());
        r.setWaybillNo(wb.getWaybillNo());
        r.setCustomerId(wb.getCustomerId());
        r.setCustomerName(wb.getCustomerName());
        r.setAmount(nz(wb.getTotalAmount()));
        r.setPaidAmount(BigDecimal.ZERO);
        r.setStatus("待对账");
        r.setSource("运单签收");
        receivableMapper.insert(r);
    }

    private void createPayable(Waybill wb) {
        Long exists = payableMapper.selectCount(new LambdaQueryWrapper<Payable>()
                .eq(Payable::getWaybillId, wb.getId()));
        if (exists != null && exists > 0) return;
        Payable p = new Payable();
        p.setWaybillId(wb.getId());
        p.setWaybillNo(wb.getWaybillNo());
        p.setCarrierId(wb.getCarrierId());
        p.setDriverId(wb.getDriverId());
        p.setPayeeName(wb.getCarrierName() == null ? wb.getDriverName() : wb.getCarrierName());
        p.setAmount(nz(wb.getPayableAmount()));
        p.setPaidAmount(BigDecimal.ZERO);
        p.setStatus("待对账");
        payableMapper.insert(p);
    }

    /** 改单：保留版本快照，重新计费，状态在途后禁止改单。 */
    @Transactional
    public Waybill update(Long id, Waybill patch, String changeReason) {
        Waybill old = waybillMapper.selectById(id);
        if (old == null) throw BizException.notFound("运单不存在");
        if (List.of("已签收", "待对账", "已对账待开票", "已结算", "已完结", "已取消").contains(old.getStatus())) {
            throw new BizException("运单已进入「" + old.getStatus() + "」，不允许改单");
        }
        try {
            WaybillVersion version = new WaybillVersion();
            version.setWaybillId(id);
            version.setVersion(1);
            version.setSnapshotJson(objectMapper.writeValueAsString(old));
            version.setChangeReason(changeReason == null ? "改单" : changeReason);
            versionMapper.insert(version);
        } catch (Exception ignored) {
        }
        patch.setId(id);
        patch.setWaybillNo(old.getWaybillNo());
        patch.setStatus(old.getStatus());
        patch.setCreatedAt(null);
        if (patch.getWeight() != null || patch.getVolume() != null || patch.getDestination() != null) {
            PriceRequest req = new PriceRequest();
            req.setCustomerId(old.getCustomerId());
            req.setOrigin(patch.getOrigin() == null ? old.getOrigin() : patch.getOrigin());
            req.setDestination(patch.getDestination() == null ? old.getDestination() : patch.getDestination());
            req.setWeight(patch.getWeight() == null ? old.getWeight() : patch.getWeight());
            req.setVolume(patch.getVolume() == null ? old.getVolume() : patch.getVolume());
            req.setVehicleType(patch.getVehicleType() == null ? old.getVehicleType() : patch.getVehicleType());
            req.setCodAmount(patch.getCodAmount() == null ? old.getCodAmount() : patch.getCodAmount());
            Map<String, Object> price = pricingService.quote(req, TenantContext.requireTenantId(), true);
            patch.setTotalAmount(dec(price.get("totalAmount")));
            patch.setReceivableAmount(dec(price.get("totalAmount")));
            patch.setPayableAmount(PricingService.money(dec(price.get("totalAmount")).multiply(new BigDecimal("0.70"))));
            patch.setGrossProfit(PricingService.money(patch.getTotalAmount().subtract(patch.getPayableAmount())));
            patch.setRuleVersion(String.valueOf(price.get("ruleVersion")));
        }
        waybillMapper.updateById(patch);
        Waybill updated = waybillMapper.selectById(id);
        log(id, old.getStatus(), updated.getStatus(), "update", changeReason, TenantContext.username());
        auditService.log("运单改单", updated.getWaybillNo(), changeReason);
        return updated;
    }

    public Map<String, Object> timeline(Long id) {
        Waybill wb = waybillMapper.selectById(id);
        if (wb == null) throw BizException.notFound("运单不存在");
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("waybill", wb);
        m.put("items", itemMapper.selectList(new LambdaQueryWrapper<WaybillItem>().eq(WaybillItem::getWaybillId, id)));
        m.put("photos", photoMapper.selectList(new LambdaQueryWrapper<WaybillPhoto>()
                .eq(WaybillPhoto::getWaybillId, id).orderByAsc(WaybillPhoto::getSortNo)));
        m.put("statusLogs", logMapper.selectList(new LambdaQueryWrapper<WaybillStatusLog>()
                .eq(WaybillStatusLog::getWaybillId, id).orderByAsc(WaybillStatusLog::getId)));
        m.put("nodes", nodeMapper.selectList(new LambdaQueryWrapper<TrackNode>()
                .eq(TrackNode::getWaybillId, id).orderByAsc(TrackNode::getId)));
        m.put("sign", signMapper.selectOne(new LambdaQueryWrapper<SignRecord>()
                .eq(SignRecord::getWaybillId, id).orderByDesc(SignRecord::getId).last("limit 1")));
        m.put("receipt", receiptMapper.selectOne(new LambdaQueryWrapper<Receipt>()
                .eq(Receipt::getWaybillId, id).orderByDesc(Receipt::getId).last("limit 1")));
        m.put("dispatch", dispatchMapper.selectOne(new LambdaQueryWrapper<Dispatch>()
                .eq(Dispatch::getWaybillId, id).orderByDesc(Dispatch::getId).last("limit 1")));
        m.put("receivable", receivableMapper.selectOne(new LambdaQueryWrapper<Receivable>()
                .eq(Receivable::getWaybillId, id).orderByDesc(Receivable::getId).last("limit 1")));
        m.put("payable", payableMapper.selectOne(new LambdaQueryWrapper<Payable>()
                .eq(Payable::getWaybillId, id).orderByDesc(Payable::getId).last("limit 1")));
        m.put("versions", versionMapper.selectList(new LambdaQueryWrapper<WaybillVersion>()
                .eq(WaybillVersion::getWaybillId, id).orderByAsc(WaybillVersion::getId)));
        return m;
    }

    public void log(Long waybillId, String from, String to, String action, String reason, String operator) {
        WaybillStatusLog log = new WaybillStatusLog();
        log.setWaybillId(waybillId);
        log.setFromStatus(from);
        log.setToStatus(to);
        log.setAction(action);
        log.setReason(reason);
        log.setOperator(operator == null ? TenantContext.username() : operator);
        logMapper.insert(log);
    }

    private String nodeOf(String status) {
        return switch (status) {
            case "已派车" -> "派车通知";
            case "到达待派送" -> "到达通知";
            case "已签收" -> "签收通知";
            case "待对账" -> "对账通知";
            case "已完结" -> "完结通知";
            default -> "状态变更";
        };
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static BigDecimal dec(Object v) {
        if (v == null) return BigDecimal.ZERO;
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private static String str(Object v) {
        return v == null ? null : String.valueOf(v);
    }
}
