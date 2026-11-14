package com.quantaida.wuliu.service.report;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.wuliu.domain.entity.*;
import com.quantaida.wuliu.domain.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** 租户工作台：今日新增、在途、待调度、异常、今日营收、欠款客户、到期资质、状态分布、近 7 日趋势。 */
@Service
@RequiredArgsConstructor
public class DashboardService {
    private final WaybillMapper waybillMapper;
    private final ExceptionOrderMapper exceptionMapper;
    private final ReceivableMapper receivableMapper;
    private final CustomerMapper customerMapper;
    private final VehicleMapper vehicleMapper;
    private final DriverMapper driverMapper;
    private final DispatchMapper dispatchMapper;

    public Map<String, Object> tenantDashboard() {
        LocalDateTime dayStart = LocalDate.now().atStartOfDay();
        List<Waybill> waybills = waybillMapper.selectList(new LambdaQueryWrapper<>());
        long todayNew = waybills.stream().filter(w -> w.getCreatedAt() != null && w.getCreatedAt().isAfter(dayStart)).count();
        long inTransit = waybills.stream().filter(w -> List.of("运输中", "中转中", "已提货", "到达待派送", "派送中").contains(w.getStatus())).count();
        long pendingDispatch = waybills.stream().filter(w -> List.of("已受理", "待调度").contains(w.getStatus())).count();
        long exceptions = exceptionMapper.selectCount(new LambdaQueryWrapper<ExceptionOrder>()
                .eq(ExceptionOrder::getStatus, "待处理"));
        BigDecimal todayRevenue = receivableMapper.selectList(new LambdaQueryWrapper<Receivable>()
                        .ge(Receivable::getCreatedAt, dayStart)).stream()
                .map(r -> r.getAmount() == null ? BigDecimal.ZERO : r.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<Receivable> unpaid = receivableMapper.selectList(new LambdaQueryWrapper<Receivable>()
                .in(Receivable::getStatus, "待对账", "客户已确认", "已开票", "部分收款", "有异议", "已挂账"));
        Map<Long, BigDecimal> debtByCustomer = new LinkedHashMap<>();
        for (Receivable r : unpaid) {
            if (r.getCustomerId() == null) continue;
            debtByCustomer.merge(r.getCustomerId(),
                    (r.getAmount() == null ? BigDecimal.ZERO : r.getAmount()).subtract(r.getPaidAmount() == null ? BigDecimal.ZERO : r.getPaidAmount()),
                    BigDecimal::add);
        }
        List<Map<String, Object>> debtCustomers = new ArrayList<>();
        debtByCustomer.entrySet().stream().sorted((a, b) -> b.getValue().compareTo(a.getValue())).limit(5).forEach(e -> {
            Customer c = customerMapper.selectById(e.getKey());
            debtCustomers.add(Map.of("customerName", c == null ? "-" : c.getName(), "unpaid", e.getValue()));
        });

        Map<String, Integer> statusDistribution = new LinkedHashMap<>();
        for (Waybill w : waybills) statusDistribution.merge(w.getStatus(), 1, Integer::sum);

        List<Map<String, Object>> trend = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            long count = waybills.stream().filter(w -> w.getCreatedAt() != null && w.getCreatedAt().toLocalDate().equals(day)).count();
            BigDecimal amount = waybills.stream().filter(w -> w.getCreatedAt() != null && w.getCreatedAt().toLocalDate().equals(day))
                    .map(w -> w.getReceivableAmount() == null ? BigDecimal.ZERO : w.getReceivableAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            trend.add(Map.of("date", day.format(fmt), "tickets", count, "amount", amount));
        }

        long expiring = countExpiring();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("todayNew", todayNew);
        m.put("inTransit", inTransit);
        m.put("pendingDispatch", pendingDispatch);
        m.put("pendingException", exceptions);
        m.put("todayRevenue", todayRevenue);
        m.put("unpaidTotal", unpaid.stream()
                .map(r -> (r.getAmount() == null ? BigDecimal.ZERO : r.getAmount())
                        .subtract(r.getPaidAmount() == null ? BigDecimal.ZERO : r.getPaidAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        m.put("debtCustomers", debtCustomers);
        m.put("expiringQualifications", expiring);
        m.put("statusDistribution", statusDistribution);
        m.put("trend", trend);
        m.put("totalWaybills", waybills.size());
        m.put("pendingDispatchList", waybills.stream()
                .filter(w -> "待调度".equals(w.getStatus())).limit(5)
                .map(w -> Map.of("id", w.getId(), "waybillNo", w.getWaybillNo(), "origin", w.getOrigin(), "destination", w.getDestination()))
                .toList());
        return m;
    }

    public long countExpiring() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limit = now.plusDays(30);
        long vehicles = vehicleMapper.selectList(new LambdaQueryWrapper<Vehicle>()
                        .isNotNull(Vehicle::getTransportExpire).le(Vehicle::getTransportExpire, limit)).size();
        long drivers = driverMapper.selectList(new LambdaQueryWrapper<Driver>()
                        .isNotNull(Driver::getQualificationExpire).le(Driver::getQualificationExpire, limit)).size();
        return vehicles + drivers;
    }

    public List<Map<String, Object>> expiringQualifications() {
        LocalDateTime limit = LocalDateTime.now().plusDays(30);
        List<Map<String, Object>> list = new ArrayList<>();
        for (Vehicle v : vehicleMapper.selectList(new LambdaQueryWrapper<Vehicle>()
                .isNotNull(Vehicle::getTransportExpire).le(Vehicle::getTransportExpire, limit))) {
            list.add(qualification("车辆", v.getPlateNo(), "道路运输证", v.getTransportExpire()));
        }
        for (Driver d : driverMapper.selectList(new LambdaQueryWrapper<Driver>()
                .isNotNull(Driver::getQualificationExpire).le(Driver::getQualificationExpire, limit))) {
            list.add(qualification("司机", d.getName(), "从业资格证", d.getQualificationExpire()));
        }
        return list;
    }

    private Map<String, Object> qualification(String type, String subject, String cert, LocalDateTime expire) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("type", type);
        m.put("subject", subject);
        m.put("cert", cert);
        m.put("expireDate", expire == null ? null : expire.toLocalDate());
        long days = expire == null ? 0 : java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), expire);
        m.put("daysLeft", days);
        m.put("expired", days < 0);
        m.put("level", days < 0 ? "已过期" : days <= 1 ? "1 天内" : days <= 7 ? "7 天内" : "30 天内");
        return m;
    }
}
