package com.quantaida.wuliu.service.track;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.wuliu.common.context.TenantContext;
import com.quantaida.wuliu.domain.entity.*;
import com.quantaida.wuliu.domain.mapper.*;
import com.quantaida.wuliu.service.message.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 超时扫描：待支付 2h、待调度 2h、派车未提货 +2h、在途 4h、回单 24h、对账逾期 3 天、轨迹 30 分钟断点。
 * 到点生成提醒/异常单，供卡滞看板与客服跟进。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TimeoutService {
    private final WaybillMapper waybillMapper;
    private final DispatchMapper dispatchMapper;
    private final ReceiptMapper receiptMapper;
    private final TrackPointMapper pointMapper;
    private final ExceptionOrderMapper exceptionMapper;
    private final MessageService messageService;

    @Scheduled(fixedDelay = 10 * 60 * 1000L, initialDelay = 60 * 1000L)
    public void scheduledScan() {
        try {
            Map<String, Object> result = scan();
            log.info("超时扫描完成：{}", result.get("findings"));
        } catch (Exception e) {
            log.warn("超时扫描失败：{}", e.getMessage());
        }
    }

    public Map<String, Object> scan() {
        List<Waybill> all = TenantContext.runWithoutTenant(() -> waybillMapper.selectList(new LambdaQueryWrapper<>()));
        List<Map<String, Object>> findings = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (Waybill wb : all) {
            TenantContext.runAs(wb.getTenantId(), () -> {
                if ("待支付".equals(wb.getStatus()) && wb.getCreatedAt() != null
                        && wb.getCreatedAt().plusHours(2).isBefore(now)) {
                    findings.add(finding(wb, "待支付超 2 小时", "已提醒客户支付，超时未支付将自动释放运力"));
                }
                if (List.of("已受理", "待调度").contains(wb.getStatus()) && wb.getUpdatedAt() != null
                        && wb.getUpdatedAt().plusHours(2).isBefore(now)) {
                    findings.add(finding(wb, "待调度超 2 小时", "调度看板已高亮，超 4 小时升级主管"));
                }
                if ("已派车".equals(wb.getStatus()) && wb.getPickupTime() != null
                        && wb.getPickupTime().plusHours(2).isBefore(now)) {
                    findings.add(finding(wb, "派车后未提货超 2 小时", "请调度确认改派并核算空驶费"));
                    createException(wb, "延误", "派车后超 2 小时未提货");
                }
                if ("运输中".equals(wb.getStatus()) && wb.getDeliveryTime() != null
                        && wb.getDeliveryTime().plusHours(4).isBefore(now)) {
                    findings.add(finding(wb, "在途超预计到达 4 小时", "已生成延误异常单，客服介入"));
                    createException(wb, "延误", "超过预计到达时间 4 小时");
                }
                if ("已签收".equals(wb.getStatus()) && wb.getSignedAt() != null
                        && wb.getSignedAt().plusHours(24).isBefore(now)) {
                    Long receipts = receiptMapper.selectCount(new LambdaQueryWrapper<Receipt>()
                            .eq(Receipt::getWaybillId, wb.getId()));
                    if (receipts == null || receipts == 0) {
                        findings.add(finding(wb, "签收后 24 小时未上传回单", "已阻断结算并提醒司机/客服"));
                    }
                }
                if ("待对账".equals(wb.getStatus()) && wb.getSignedAt() != null
                        && wb.getSignedAt().plusDays(3).isBefore(now)) {
                    findings.add(finding(wb, "对账逾期 3 天", "已进入催收清单，暂停该客户月结"));
                }
                // 轨迹断点：在途且最近 30 分钟无点
                if (List.of("运输中", "中转中", "派送中").contains(wb.getStatus())) {
                    TrackPoint latest = pointMapper.selectOne(new LambdaQueryWrapper<TrackPoint>()
                            .eq(TrackPoint::getWaybillId, wb.getId())
                            .orderByDesc(TrackPoint::getReportedAt).last("limit 1"));
                    if (latest != null && latest.getReportedAt() != null
                            && latest.getReportedAt().plusMinutes(30).isBefore(now)) {
                        findings.add(finding(wb, "轨迹 30 分钟无定位点", "已推送司机确认定位权限，客服电话核实"));
                    }
                }
            });
        }
        for (Map<String, Object> f : findings) {
            try {
                messageService.notify("超时提醒", (Long) f.get("waybillId"),
                        String.valueOf(f.getOrDefault("receiverPhone", "")), String.valueOf(f.get("waybillNo")),
                        String.valueOf(f.get("finding")));
            } catch (Exception ignored) {
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("findings", findings);
        result.put("count", findings.size());
        result.put("scannedAt", now);
        return result;
    }

    private Map<String, Object> finding(Waybill wb, String type, String action) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("waybillId", wb.getId());
        m.put("waybillNo", wb.getWaybillNo());
        m.put("status", wb.getStatus());
        m.put("finding", type);
        m.put("action", action);
        m.put("receiverPhone", wb.getSenderPhone());
        return m;
    }

    private void createException(Waybill wb, String type, String desc) {
        Long exists = exceptionMapper.selectCount(new LambdaQueryWrapper<ExceptionOrder>()
                .eq(ExceptionOrder::getWaybillId, wb.getId())
                .eq(ExceptionOrder::getRecordType, type)
                .eq(ExceptionOrder::getStatus, "待处理"));
        if (exists != null && exists > 0) return;
        ExceptionOrder order = new ExceptionOrder();
        order.setWaybillId(wb.getId());
        order.setRecordType(type);
        order.setDescription(desc);
        order.setImages("[]");
        order.setStatus("待处理");
        order.setEscalated(1);
        exceptionMapper.insert(order);
    }
}
