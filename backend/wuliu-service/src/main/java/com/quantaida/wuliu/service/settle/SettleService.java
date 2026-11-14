package com.quantaida.wuliu.service.settle;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.wuliu.common.context.TenantContext;
import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.domain.entity.*;
import com.quantaida.wuliu.domain.mapper.*;import com.quantaida.wuliu.service.order.WaybillService;
import com.quantaida.wuliu.service.system.AuditService;
import com.quantaida.wuliu.service.system.NumberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** 结算：对账单、确认/异议、开票、收款核销、账龄催收、代收货款、毛利。 */
@Service
@RequiredArgsConstructor
public class SettleService {
    private final ReceivableMapper receivableMapper;
    private final PayableMapper payableMapper;
    private final StatementMapper statementMapper;
    private final StatementItemMapper itemMapper;
    private final InvoiceMapper invoiceMapper;
    private final PaymentMapper paymentMapper;
    private final CodSettleMapper codMapper;
    private final CustomerMapper customerMapper;
    private final CarrierMapper carrierMapper;
    private final WaybillMapper waybillMapper;
    private final WaybillService waybillService;
    private final NumberService numberService;
    private final AuditService auditService;

    @Transactional
    public Statement generateStatement(String type, Long partyId, LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) throw new BizException("请选择对账期间");
        if (end.isBefore(start)) throw new BizException("结束时间不能早于开始时间");
        List<Long> ids = new ArrayList<>();
        List<StatementItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        String partyName = "";
        if ("客户".equals(type)) {
            Customer customer = customerMapper.selectById(partyId);
            if (customer == null) throw new BizException("客户不存在");
            partyName = customer.getName();
            List<Receivable> list = receivableMapper.selectList(new LambdaQueryWrapper<Receivable>()
                    .eq(Receivable::getCustomerId, partyId)
                    .eq(Receivable::getStatus, "待对账")
                    .isNull(Receivable::getStatementId)
                    .between(Receivable::getCreatedAt, start, end));
            for (Receivable r : list) {
                StatementItem item = new StatementItem();
                item.setWaybillId(r.getWaybillId());
                item.setWaybillNo(r.getWaybillNo());
                item.setAmount(r.getAmount());
                item.setDifference(BigDecimal.ZERO);
                items.add(item);
                total = total.add(nz(r.getAmount()));
                ids.add(r.getId());
            }
            if (items.isEmpty()) throw new BizException("该期间没有待对账的应收单据（请确认运单已签收并生成应收）");
        } else {
            List<Payable> list;
            if (partyId != null) {
                Carrier carrier = carrierMapper.selectById(partyId);
                if (carrier == null) throw new BizException("承运商不存在");
                partyName = carrier.getName();
                list = payableMapper.selectList(new LambdaQueryWrapper<Payable>()
                        .eq(Payable::getCarrierId, partyId)
                        .eq(Payable::getStatus, "待对账")
                        .isNull(Payable::getStatementId)
                        .between(Payable::getCreatedAt, start, end));
            } else {
                list = payableMapper.selectList(new LambdaQueryWrapper<Payable>()
                        .eq(Payable::getStatus, "待对账").isNull(Payable::getStatementId)
                        .between(Payable::getCreatedAt, start, end));
                partyName = "全部承运商";
            }
            for (Payable p : list) {
                StatementItem item = new StatementItem();
                item.setWaybillId(p.getWaybillId());
                item.setWaybillNo(p.getWaybillNo());
                item.setAmount(p.getAmount());
                item.setDifference(BigDecimal.ZERO);
                items.add(item);
                total = total.add(nz(p.getAmount()));
                ids.add(p.getId());
            }
            if (items.isEmpty()) throw new BizException("该期间没有待对账的应付单据");
        }

        Statement statement = new Statement();
        statement.setStatementNo(numberService.next("DZ", TenantContext.requireTenantId()));
        statement.setRecordType(type);
        statement.setCustomerId("客户".equals(type) ? partyId : null);
        statement.setCarrierId("承运商".equals(type) ? partyId : null);
        statement.setPartyName(partyName);
        statement.setPeriodStart(start);
        statement.setPeriodEnd(end);
        statement.setTotalAmount(total);
        statement.setConfirmedAmount(BigDecimal.ZERO);
        statement.setStatus("待客户确认");
        statement.setCreatedBy(TenantContext.username());
        statementMapper.insert(statement);

        for (StatementItem item : items) {
            item.setStatementId(statement.getId());
            itemMapper.insert(item);
        }
        if ("客户".equals(type)) {
            for (Long id : ids) {
                Receivable r = receivableMapper.selectById(id);
                r.setStatus("待对账");
                r.setStatementId(statement.getId());
                receivableMapper.updateById(r);
            }
        } else {
            for (Long id : ids) {
                Payable p = payableMapper.selectById(id);
                p.setStatementId(statement.getId());
                payableMapper.updateById(p);
            }
        }
        auditService.log("生成对账单", statement.getStatementNo(), type + "：" + partyName + "，金额：" + total);
        return statement;
    }

    public Map<String, Object> statementDetail(Long id) {
        Statement statement = statementMapper.selectById(id);
        if (statement == null) throw BizException.notFound("对账单不存在");
        List<StatementItem> items = itemMapper.selectList(new LambdaQueryWrapper<StatementItem>()
                .eq(StatementItem::getStatementId, id));
        BigDecimal total = items.stream().map(i -> nz(i.getAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("statement", statement);
        m.put("items", items);
        m.put("total", total);
        m.put("differenceTotal", items.stream().map(i -> nz(i.getDifference())).reduce(BigDecimal.ZERO, BigDecimal::add));
        return m;
    }

    @Transactional
    public void confirm(Long id, boolean pass, String reason) {
        Statement statement = statementMapper.selectById(id);
        if (statement == null) throw BizException.notFound("对账单不存在");
        if (pass) {
            statement.setStatus("客户已确认");
            statement.setConfirmedAmount(statement.getTotalAmount());
            statement.setConfirmedAt(LocalDateTime.now());
        } else {
            statement.setStatus("有异议");
            statement.setDisputeReason(reason);
        }
        statementMapper.updateById(statement);
        if ("客户".equals(statement.getRecordType())) {
            List<Receivable> receivables = receivableMapper.selectList(new LambdaQueryWrapper<Receivable>()
                    .eq(Receivable::getStatementId, id));
            for (Receivable r : receivables) {
                r.setStatus(pass ? "客户已确认" : "有异议");
                receivableMapper.updateById(r);
                if (pass) {
                    try {
                        waybillService.transition(r.getWaybillId(), "statement", "客户确认对账单", null);
                    } catch (BizException ignored) {
                    }
                }
            }
        }
        auditService.log(pass ? "客户确认对账" : "客户提出异议", statement.getStatementNo(), reason);
    }

    @Transactional
    public Invoice invoice(Long statementId, String invoiceNo, String invoiceType, BigDecimal amount) {
        Statement statement = statementMapper.selectById(statementId);
        if (statement == null) throw BizException.notFound("对账单不存在");
        if (!"客户已确认".equals(statement.getStatus()) && !"已开票".equals(statement.getStatus())) {
            throw new BizException("客户尚未确认对账单，开票按钮不可用");
        }
        BigDecimal invoiced = invoiceMapper.selectList(new LambdaQueryWrapper<Invoice>()
                        .eq(Invoice::getStatementId, statementId)).stream()
                .map(i -> nz(i.getAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (amount == null || amount.signum() <= 0) throw new BizException("请填写开票金额");
        if (invoiced.add(amount).compareTo(nz(statement.getTotalAmount())) > 0) {
            throw new BizException("开票金额超过对账金额（已开 " + invoiced + " / 对账 " + statement.getTotalAmount() + "）");
        }
        Invoice invoice = new Invoice();
        invoice.setInvoiceNo(invoiceNo);
        invoice.setStatementId(statementId);
        invoice.setCustomerId(statement.getCustomerId());
        invoice.setInvoiceType(invoiceType);
        invoice.setAmount(amount);
        invoice.setStatus("已开票");
        invoice.setAppliedBy(TenantContext.username());
        invoice.setIssuedAt(LocalDateTime.now());
        invoiceMapper.insert(invoice);
        statement.setStatus("已开票");
        statementMapper.updateById(statement);
        List<Receivable> list = receivableMapper.selectList(new LambdaQueryWrapper<Receivable>()
                .eq(Receivable::getStatementId, statementId));
        for (Receivable r : list) {
            r.setStatus("已开票");
            receivableMapper.updateById(r);
            try {
                waybillService.transition(r.getWaybillId(), "invoice", "已开票", null);
            } catch (BizException ignored) {
            }
        }
        auditService.log("开票", statement.getStatementNo(), invoiceNo + " / " + amount);
        return invoice;
    }

    @Transactional
    public Payment payment(Long statementId, BigDecimal amount, String method, String differenceReason) {
        Statement statement = statementMapper.selectById(statementId);
        if (statement == null) throw BizException.notFound("对账单不存在");
        if (amount == null || amount.signum() <= 0) throw new BizException("请填写收款金额");
        BigDecimal total = nz(statement.getTotalAmount());
        BigDecimal paidBefore = paymentMapper.selectList(new LambdaQueryWrapper<Payment>()
                        .eq(Payment::getStatementId, statementId)).stream()
                .map(p -> nz(p.getAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal difference = total.subtract(paidBefore.add(amount));
        if (difference.signum() != 0 && (differenceReason == null || differenceReason.isBlank())) {
            throw new BizException("收款金额与应收不一致，请填写差额原因（差额 " + difference + " 元）");
        }
        Payment payment = new Payment();
        payment.setPaymentNo(numberService.next("SK", TenantContext.requireTenantId()));
        payment.setStatementId(statementId);
        payment.setCustomerId(statement.getCustomerId());
        payment.setAmount(amount);
        payment.setMethod(method);
        payment.setDifferenceAmount(difference);
        payment.setDifferenceReason(differenceReason);
        payment.setOperator(TenantContext.username());
        payment.setPaidAt(LocalDateTime.now());
        paymentMapper.insert(payment);

        statement.setStatus(paidBefore.add(amount).compareTo(total) >= 0 ? "已结清" : "部分收款");
        statementMapper.updateById(statement);
        List<Receivable> list = receivableMapper.selectList(new LambdaQueryWrapper<Receivable>()
                .eq(Receivable::getStatementId, statementId));
        for (Receivable r : list) {
            r.setPaidAmount(nz(r.getPaidAmount()).add(amount));
            r.setStatus(r.getPaidAmount().compareTo(nz(r.getAmount())) >= 0 ? "已结清" : "部分收款");
            receivableMapper.updateById(r);
            if ("已结清".equals(r.getStatus())) {
                try {
                    waybillService.transition(r.getWaybillId(), "settle", "收款登记完成", null);
                } catch (BizException ignored) {
                }
            }
        }
        auditService.log("收款登记", statement.getStatementNo(), amount + " / " + method);
        return payment;
    }

    public Map<String, Object> aging() {
        List<Receivable> list = receivableMapper.selectList(new LambdaQueryWrapper<Receivable>()
                .in(Receivable::getStatus, "待对账", "客户已确认", "已开票", "部分收款", "已挂账", "有异议"));
        Map<String, BigDecimal> buckets = new LinkedHashMap<>();
        buckets.put("0-30 天", BigDecimal.ZERO);
        buckets.put("31-60 天", BigDecimal.ZERO);
        buckets.put("61-90 天", BigDecimal.ZERO);
        buckets.put("90 天以上", BigDecimal.ZERO);
        List<Map<String, Object>> detail = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (Receivable r : list) {
            long days = r.getCreatedAt() == null ? 0 : ChronoUnit.DAYS.between(r.getCreatedAt(), now);
            String bucket = days <= 30 ? "0-30 天" : days <= 60 ? "31-60 天" : days <= 90 ? "61-90 天" : "90 天以上";
            BigDecimal unpaid = nz(r.getAmount()).subtract(nz(r.getPaidAmount()));
            buckets.put(bucket, buckets.get(bucket).add(unpaid));
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("customerName", r.getCustomerName());
            m.put("waybillNo", r.getWaybillNo());
            m.put("amount", r.getAmount());
            m.put("paidAmount", r.getPaidAmount());
            m.put("unpaid", unpaid);
            m.put("days", days);
            m.put("bucket", bucket);
            m.put("status", r.getStatus());
            detail.add(m);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("buckets", buckets);
        result.put("list", detail);
        result.put("total", buckets.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
        return result;
    }

    public Map<String, Object> profit() {
        List<Waybill> waybills = waybillMapper.selectList(new LambdaQueryWrapper<Waybill>()
                .isNotNull(Waybill::getReceivableAmount).orderByDesc(Waybill::getId));
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, BigDecimal> byLine = new LinkedHashMap<>();
        for (Waybill w : waybills) {
            BigDecimal receivable = nz(w.getReceivableAmount());
            BigDecimal payable = nz(w.getPayableAmount());
            BigDecimal profit = receivable.subtract(payable);
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("waybillNo", w.getWaybillNo());
            m.put("customerName", w.getCustomerName());
            m.put("origin", w.getOrigin());
            m.put("destination", w.getDestination());
            m.put("receivable", receivable);
            m.put("payable", payable);
            m.put("profit", profit);
            m.put("margin", receivable.signum() == 0 ? BigDecimal.ZERO :
                    profit.multiply(new BigDecimal("100")).divide(receivable, 2, java.math.RoundingMode.HALF_UP));
            list.add(m);
            byLine.merge(w.getOrigin() + " → " + w.getDestination(), profit, BigDecimal::add);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("list", list);
        result.put("byLine", byLine);
        return result;
    }

    public List<CodSettle> codList() {
        return codMapper.selectList(new LambdaQueryWrapper<CodSettle>().orderByDesc(CodSettle::getId));
    }

    @Transactional
    public void codSettle(Long id, String channelNo) {
        CodSettle cod = codMapper.selectById(id);
        if (cod == null) throw BizException.notFound("代收记录不存在");
        cod.setStatus("已回款");
        cod.setChannelNo(channelNo);
        cod.setSettledAt(LocalDateTime.now());
        codMapper.updateById(cod);
        auditService.log("代收回款", cod.getWaybillNo(), "通道流水：" + channelNo);
    }

    public List<Receivable> receivables() {
        return receivableMapper.selectList(new LambdaQueryWrapper<Receivable>().orderByDesc(Receivable::getId));
    }

    public List<Payable> payables() {
        return payableMapper.selectList(new LambdaQueryWrapper<Payable>().orderByDesc(Payable::getId));
    }

    public List<Statement> statements() {
        return statementMapper.selectList(new LambdaQueryWrapper<Statement>().orderByDesc(Statement::getId));
    }

    public List<Invoice> invoices() {
        return invoiceMapper.selectList(new LambdaQueryWrapper<Invoice>().orderByDesc(Invoice::getId));
    }

    public List<Payment> payments() {
        return paymentMapper.selectList(new LambdaQueryWrapper<Payment>().orderByDesc(Payment::getId));
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
