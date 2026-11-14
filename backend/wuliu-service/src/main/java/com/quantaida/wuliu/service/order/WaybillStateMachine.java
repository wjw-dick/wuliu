package com.quantaida.wuliu.service.order;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.domain.entity.Receipt;
import com.quantaida.wuliu.domain.entity.Receivable;
import com.quantaida.wuliu.domain.entity.Waybill;
import com.quantaida.wuliu.domain.entity.WaybillStatusLog;
import com.quantaida.wuliu.domain.mapper.ReceiptMapper;
import com.quantaida.wuliu.domain.mapper.ReceivableMapper;
import com.quantaida.wuliu.domain.mapper.WaybillStatusLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 运单状态机：唯一状态迁移入口，禁止 Controller 直接改 status。
 * 相邻路径校验、越级拦截（中文原因）、迁移日志（谁/何时/由何改为何）。
 */
@Service
@RequiredArgsConstructor
public class WaybillStateMachine {
    private final WaybillStatusLogMapper logMapper;
    private final ReceiptMapper receiptMapper;
    private final ReceivableMapper receivableMapper;

    public static final Map<String, String[]> ACTIONS = Map.ofEntries(
            Map.entry("quote", new String[]{"待报价", "待客户确认"}),
            Map.entry("confirm", new String[]{"待客户确认", "已受理"}),
            Map.entry("pay", new String[]{"待支付", "已受理"}),
            Map.entry("submit", new String[]{"已受理", "待调度"}),
            Map.entry("dispatch", new String[]{"待调度", "已派车"}),
            Map.entry("pickup", new String[]{"已派车", "已提货"}),
            Map.entry("ship", new String[]{"已提货", "运输中"}),
            Map.entry("transfer", new String[]{"运输中", "中转中"}),
            Map.entry("depart", new String[]{"中转中", "运输中"}),
            Map.entry("arrive", new String[]{"运输中", "到达待派送"}),
            Map.entry("deliver", new String[]{"到达待派送", "派送中"}),
            Map.entry("sign", new String[]{"派送中", "已签收"}),
            Map.entry("receipt", new String[]{"已签收", "待对账"}),
            Map.entry("statement", new String[]{"待对账", "已对账待开票"}),
            Map.entry("invoice", new String[]{"已对账待开票", "已结算"}),
            Map.entry("settle", new String[]{"已结算", "已完结"}),
            Map.entry("finish", new String[]{"待对账", "已完结"}),
            Map.entry("reject", new String[]{"派送中", "退回中"}),
            Map.entry("returnSign", new String[]{"退回中", "已退回"}),
            Map.entry("returnFinish", new String[]{"已退回", "已完结"}),
            Map.entry("claimClose", new String[]{"理赔中", "已完结"})
    );

    public static final Set<String> TERMINAL = Set.of("已完结", "已取消", "已退回");

    public Target resolve(Waybill waybill, String action, String payloadPreviousStatus) {
        if ("cancel".equals(action)) {
            if (TERMINAL.contains(waybill.getStatus())) {
                throw new BizException("运单当前状态为「" + waybill.getStatus() + "」，不能取消");
            }
            if ("已签收".equals(waybill.getStatus())) {
                throw new BizException("运单已签收，请走拒收退回或理赔流程");
            }
            return new Target("已取消", "已取消");
        }
        if ("exception".equals(action)) {
            if (TERMINAL.contains(waybill.getStatus())) {
                throw new BizException("运单当前状态为「" + waybill.getStatus() + "」，不能上报异常");
            }
            return new Target("异常处理中", "异常处理中");
        }
        if ("resolve".equals(action)) {
            if (!"异常处理中".equals(waybill.getStatus())) {
                throw new BizException("运单不在「异常处理中」，无需恢复");
            }
            String back = payloadPreviousStatus;
            if (back == null || back.isBlank()) {
                WaybillStatusLog last = logMapper.selectOne(new LambdaQueryWrapper<WaybillStatusLog>()
                        .eq(WaybillStatusLog::getWaybillId, waybill.getId())
                        .eq(WaybillStatusLog::getToStatus, "异常处理中")
                        .orderByDesc(WaybillStatusLog::getId).last("limit 1"));
                back = last == null ? "运输中" : last.getFromStatus();
            }
            return new Target(back, "异常处理中 → " + back);
        }
        if ("claim".equals(action) || "理赔中".equals(waybill.getStatus())) {
            if ("claim".equals(action)) return new Target("理赔中", "理赔中");
        }
        String[] rule = ACTIONS.get(action);
        if (rule == null) {
            throw new BizException("不支持的运单操作：" + action);
        }
        if (!rule[0].equals(waybill.getStatus())) {
            throw new BizException("运单当前状态为「" + waybill.getStatus() + "」，不能执行该操作（需要状态「" + rule[0] + "」）");
        }
        String target = rule[1];
        assertGuards(waybill, action, target);
        return new Target(target, action);
    }

    /** 闭环完成标志：回单齐全 + 收款依据齐全，缺一不可置为已完结。 */
    private void assertGuards(Waybill waybill, String action, String target) {
        if (target.equals("已完结") && "settle".equals(action)) {
            Long receipts = receiptMapper.selectCount(new LambdaQueryWrapper<Receipt>()
                    .eq(Receipt::getWaybillId, waybill.getId())
                    .eq(Receipt::getStatus, "已通过"));
            if (receipts == null || receipts == 0) {
                throw new BizException("该运单缺少已审核回单，不能置为已完结");
            }
            List<Receivable> list = receivableMapper.selectList(new LambdaQueryWrapper<Receivable>()
                    .eq(Receivable::getWaybillId, waybill.getId()));
            BigDecimal paid = list.stream().map(r -> nz(r.getPaidAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal amount = list.stream().map(r -> nz(r.getAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);
            if (amount.signum() > 0 && paid.compareTo(amount) < 0) {
                throw new BizException("收款金额未覆盖应收（已收 " + paid + " / 应收 " + amount + "），差额需挂账并填写原因");
            }
        }
        if (target.equals("已完结") && "finish".equals(action)) {
            Long receipts = receiptMapper.selectCount(new LambdaQueryWrapper<Receipt>()
                    .eq(Receipt::getWaybillId, waybill.getId())
                    .eq(Receipt::getStatus, "已通过"));
            if (receipts == null || receipts == 0) {
                throw new BizException("该运单缺少已审核回单，不能置为已完结");
            }
        }
        if (target.equals("已对账待开票") && "statement".equals(action)) {
            Long count = receivableMapper.selectCount(new LambdaQueryWrapper<Receivable>()
                    .eq(Receivable::getWaybillId, waybill.getId()));
            if (count == null || count == 0) {
                throw new BizException("该运单尚未生成应收，不能提交对账");
            }
        }
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    public record Target(String status, String actionLabel) {
    }
}
