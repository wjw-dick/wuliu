package com.quantaida.wuliu.service.report;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.wuliu.domain.entity.*;
import com.quantaida.wuliu.domain.mapper.*;
import com.quantaida.wuliu.service.system.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** 报表中心：业务量、营收毛利、线路盈亏、时效达成、异常率、司机绩效、客户贡献、承运商评分。 */
@Service
@RequiredArgsConstructor
public class ReportService {
    private final WaybillMapper waybillMapper;
    private final TrackNodeMapper nodeMapper;
    private final ExceptionOrderMapper exceptionMapper;
    private final ClaimMapper claimMapper;
    private final DriverMapper driverMapper;
    private final CarrierMapper carrierMapper;
    private final AuditService auditService;

    public Map<String, Object> report(String type) {
        List<Waybill> all = waybillMapper.selectList(new LambdaQueryWrapper<Waybill>().orderByDesc(Waybill::getId));
        return switch (type) {
            case "volume" -> volume(all);
            case "revenue" -> revenue(all);
            case "line-profit" -> lineProfit(all);
            case "timeliness" -> timeliness(all);
            case "exception" -> exceptionRate(all);
            case "driver" -> driverPerformance(all);
            case "customer" -> customerContribution(all);
            case "carrier" -> carrierScore(all);
            default -> throw new com.quantaida.wuliu.common.exception.BizException("不支持的报表类型：" + type);
        };
    }

    private Map<String, Object> volume(List<Waybill> all) {
        Map<String, int[]> byDay = new TreeMap<>();
        Map<String, Integer> byStatus = new LinkedHashMap<>();
        BigDecimal tons = BigDecimal.ZERO;
        BigDecimal cbm = BigDecimal.ZERO;
        for (Waybill w : all) {
            String day = w.getCreatedAt() == null ? LocalDate.now().toString() : w.getCreatedAt().toLocalDate().toString();
            int[] arr = byDay.computeIfAbsent(day, k -> new int[2]);
            arr[0]++;
            arr[1] += w.getPieces() == null ? 0 : w.getPieces();
            byStatus.merge(w.getStatus(), 1, Integer::sum);
            tons = tons.add(w.getWeight() == null ? BigDecimal.ZERO : w.getWeight());
            cbm = cbm.add(w.getVolume() == null ? BigDecimal.ZERO : w.getVolume());
        }
        List<Map<String, Object>> trend = new ArrayList<>();
        byDay.forEach((day, arr) -> trend.add(Map.of("date", day, "tickets", arr[0], "pieces", arr[1])));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("totalTickets", all.size());
        m.put("totalWeight", tons.setScale(2, RoundingMode.HALF_UP));
        m.put("totalVolume", cbm.setScale(2, RoundingMode.HALF_UP));
        m.put("byStatus", byStatus);
        m.put("trend", trend);
        return m;
    }

    private Map<String, Object> revenue(List<Waybill> all) {
        BigDecimal receivable = BigDecimal.ZERO;
        BigDecimal payable = BigDecimal.ZERO;
        Map<String, BigDecimal> byMonth = new TreeMap<>();
        for (Waybill w : all) {
            BigDecimal r = w.getReceivableAmount() == null ? BigDecimal.ZERO : w.getReceivableAmount();
            BigDecimal p = w.getPayableAmount() == null ? BigDecimal.ZERO : w.getPayableAmount();
            receivable = receivable.add(r);
            payable = payable.add(p);
            String month = w.getCreatedAt() == null ? "" : w.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            byMonth.merge(month, r, BigDecimal::add);
        }
        List<Map<String, Object>> trend = new ArrayList<>();
        byMonth.forEach((m, v) -> trend.add(Map.of("month", m, "revenue", v)));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("receivable", receivable);
        result.put("payable", payable);
        result.put("grossProfit", receivable.subtract(payable));
        result.put("margin", receivable.signum() == 0 ? 0 :
                receivable.subtract(payable).multiply(new BigDecimal("100")).divide(receivable, 2, RoundingMode.HALF_UP));
        result.put("trend", trend);
        return result;
    }

    private Map<String, Object> lineProfit(List<Waybill> all) {
        Map<String, Map<String, Object>> lines = new LinkedHashMap<>();
        for (Waybill w : all) {
            String key = (w.getOrigin() == null ? "" : w.getOrigin()) + " → " + (w.getDestination() == null ? "" : w.getDestination());
            Map<String, Object> line = lines.computeIfAbsent(key, k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("line", k);
                m.put("tickets", 0);
                m.put("receivable", BigDecimal.ZERO);
                m.put("payable", BigDecimal.ZERO);
                m.put("profit", BigDecimal.ZERO);
                return m;
            });
            line.put("tickets", (int) line.get("tickets") + 1);
            line.put("receivable", ((BigDecimal) line.get("receivable")).add(nz(w.getReceivableAmount())));
            line.put("payable", ((BigDecimal) line.get("payable")).add(nz(w.getPayableAmount())));
            line.put("profit", ((BigDecimal) line.get("profit")).add(nz(w.getReceivableAmount()).subtract(nz(w.getPayableAmount()))));
        }
        return Map.of("lines", new ArrayList<>(lines.values()));
    }

    private Map<String, Object> timeliness(List<Waybill> all) {
        long signed = all.stream().filter(w -> w.getSignedAt() != null).count();
        long onTime = all.stream().filter(w -> w.getSignedAt() != null && w.getDeliveryTime() != null
                && w.getSignedAt().isBefore(w.getDeliveryTime().plusHours(4))).count();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("signedCount", signed);
        m.put("onTimeCount", onTime);
        m.put("rate", signed == 0 ? 0 : BigDecimal.valueOf(onTime * 100.0 / signed).setScale(2, RoundingMode.HALF_UP));
        return m;
    }

    private Map<String, Object> exceptionRate(List<Waybill> all) {
        long exceptions = exceptionMapper.selectCount(new LambdaQueryWrapper<>());
        long claims = claimMapper.selectCount(new LambdaQueryWrapper<>());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", all.size());
        m.put("exceptionCount", exceptions);
        m.put("claimCount", claims);
        m.put("rate", all.isEmpty() ? 0 : BigDecimal.valueOf(exceptions * 100.0 / all.size()).setScale(2, RoundingMode.HALF_UP));
        return m;
    }

    private Map<String, Object> driverPerformance(List<Waybill> all) {
        Map<String, Map<String, Object>> drivers = new LinkedHashMap<>();
        for (Waybill w : all) {
            if (w.getDriverName() == null) continue;
            Map<String, Object> d = drivers.computeIfAbsent(w.getDriverName(), k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("driverName", k);
                m.put("tickets", 0);
                m.put("weight", BigDecimal.ZERO);
                m.put("signed", 0);
                return m;
            });
            d.put("tickets", (int) d.get("tickets") + 1);
            d.put("weight", ((BigDecimal) d.get("weight")).add(nz(w.getWeight())));
            if (w.getSignedAt() != null) d.put("signed", (int) d.get("signed") + 1);
        }
        return Map.of("list", new ArrayList<>(drivers.values()));
    }

    private Map<String, Object> customerContribution(List<Waybill> all) {
        Map<String, Map<String, Object>> customers = new LinkedHashMap<>();
        for (Waybill w : all) {
            String name = w.getCustomerName() == null ? "散客" : w.getCustomerName();
            Map<String, Object> c = customers.computeIfAbsent(name, k -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("customerName", k);
                m.put("tickets", 0);
                m.put("amount", BigDecimal.ZERO);
                m.put("profit", BigDecimal.ZERO);
                return m;
            });
            c.put("tickets", (int) c.get("tickets") + 1);
            c.put("amount", ((BigDecimal) c.get("amount")).add(nz(w.getReceivableAmount())));
            c.put("profit", ((BigDecimal) c.get("profit")).add(nz(w.getReceivableAmount()).subtract(nz(w.getPayableAmount()))));
        }
        return Map.of("list", new ArrayList<>(customers.values()));
    }

    private Map<String, Object> carrierScore(List<Waybill> all) {
        List<Carrier> carriers = carrierMapper.selectList(new LambdaQueryWrapper<Carrier>().orderByDesc(Carrier::getScore));
        List<Map<String, Object>> list = new ArrayList<>();
        for (Carrier c : carriers) {
            long tickets = all.stream().filter(w -> c.getId().equals(w.getCarrierId())).count();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("carrierName", c.getName());
            m.put("score", c.getScore());
            m.put("status", c.getStatus());
            m.put("tickets", tickets);
            list.add(m);
        }
        return Map.of("list", list);
    }

    public Map<String, Object> export(String type, String purpose) {
        if (purpose == null || purpose.isBlank()) {
            throw new com.quantaida.wuliu.common.exception.BizException("导出前请填写用途");
        }
        Map<String, Object> data = report(type);
        Object rows = data.getOrDefault("list", data.getOrDefault("trend", data.getOrDefault("lines", List.of())));
        int count = rows instanceof List<?> l ? l.size() : 1;
        auditService.log("报表导出", type, "条数：" + count + "，用途：" + purpose);
        return Map.of("type", type, "rows", rows, "count", count, "exportedAt", new Date());
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
