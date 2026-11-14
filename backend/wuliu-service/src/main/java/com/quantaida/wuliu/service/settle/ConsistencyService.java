package com.quantaida.wuliu.service.settle;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.wuliu.common.context.TenantContext;
import com.quantaida.wuliu.domain.entity.*;
import com.quantaida.wuliu.domain.mapper.*;
import com.quantaida.wuliu.service.system.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 日终一致性校对：运单应收 / 应收单 / 派车单 / 结算单金额与状态比对。
 * 差异生成修复任务单，处置闭环留痕；正常时差异为 0。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsistencyService {
    private final WaybillMapper waybillMapper;
    private final ReceivableMapper receivableMapper;
    private final PayableMapper payableMapper;
    private final DispatchMapper dispatchMapper;
    private final RepairTaskMapper repairTaskMapper;
    private final AuditService auditService;

    @Scheduled(cron = "0 30 2 * * ?")
    public void dailyCheck() {
        Map<String, Object> result = check();
        log.info("日终一致性校对完成，差异 {} 条", result.get("differences"));
    }

    public Map<String, Object> check() {
        List<Waybill> waybills = TenantContext.runWithoutTenant(() ->
                waybillMapper.selectList(new LambdaQueryWrapper<Waybill>()
                        .in(Waybill::getStatus, "已签收", "待对账", "已对账待开票", "已结算", "已完结")));
        List<Map<String, Object>> diffs = new ArrayList<>();
        for (Waybill wb : waybills) {
            TenantContext.runAs(wb.getTenantId(), () -> {
                List<Receivable> receivables = receivableMapper.selectList(new LambdaQueryWrapper<Receivable>()
                        .eq(Receivable::getWaybillId, wb.getId()));
                BigDecimal receivableSum = receivables.stream().map(r -> nz(r.getAmount())).reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal waybillAmount = nz(wb.getReceivableAmount());
                if (receivableSum.signum() > 0 && receivableSum.compareTo(waybillAmount) != 0) {
                    diffs.add(diff(wb, "应收金额不一致", "运单应收 " + waybillAmount + "，应收单合计 " + receivableSum));
                }
                if (receivables.isEmpty() && waybillAmount.signum() > 0) {
                    diffs.add(diff(wb, "缺少应收单", "运单已签收但未生成应收单"));
                }
                List<Payable> payables = payableMapper.selectList(new LambdaQueryWrapper<Payable>()
                        .eq(Payable::getWaybillId, wb.getId()));
                if (payables.isEmpty() && nz(wb.getPayableAmount()).signum() > 0) {
                    diffs.add(diff(wb, "缺少应付单", "运单已签收但未生成应付单"));
                }
                Dispatch dispatch = dispatchMapper.selectOne(new LambdaQueryWrapper<Dispatch>()
                        .eq(Dispatch::getWaybillId, wb.getId()).orderByDesc(Dispatch::getId).last("limit 1"));
                if (dispatch == null && List.of("已签收", "待对账", "已对账待开票", "已结算", "已完结").contains(wb.getStatus())) {
                    diffs.add(diff(wb, "缺少派车单", "运单已签收但无派车单记录"));
                }
            });
        }
        for (Map<String, Object> d : diffs) {
            Long waybillId = (Long) d.get("waybillId");
            Long exists = repairTaskMapper.selectCount(new LambdaQueryWrapper<RepairTask>()
                    .eq(RepairTask::getWaybillId, waybillId).eq(RepairTask::getStatus, "待处理"));
            if (exists == null || exists == 0) {
                RepairTask task = new RepairTask();
                task.setRecordType(String.valueOf(d.get("type")));
                task.setWaybillId(waybillId);
                task.setDetail(String.valueOf(d.get("detail")));
                task.setStatus("待处理");
                repairTaskMapper.insert(task);
            }
        }
        auditService.log("日终一致性校对", "全量", "差异 " + diffs.size() + " 条");
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("differences", diffs.size());
        result.put("list", diffs);
        result.put("checkedAt", LocalDateTime.now());
        return result;
    }

    public List<RepairTask> tasks() {
        return repairTaskMapper.selectList(new LambdaQueryWrapper<RepairTask>().orderByDesc(RepairTask::getId));
    }

    public void handle(Long id, String detail) {
        RepairTask task = repairTaskMapper.selectById(id);
        if (task == null) throw com.quantaida.wuliu.common.exception.BizException.notFound("修复任务不存在");
        task.setStatus("已处理");
        task.setDetail((task.getDetail() == null ? "" : task.getDetail()) + "；处置：" + detail);
        task.setHandledAt(LocalDateTime.now());
        repairTaskMapper.updateById(task);
        auditService.log("一致性修复", String.valueOf(task.getWaybillId()), detail);
    }

    private Map<String, Object> diff(Waybill wb, String type, String detail) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("waybillId", wb.getId());
        m.put("waybillNo", wb.getWaybillNo());
        m.put("type", type);
        m.put("detail", detail);
        m.put("tenantId", wb.getTenantId());
        return m;
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
