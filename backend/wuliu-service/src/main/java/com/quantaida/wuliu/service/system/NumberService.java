package com.quantaida.wuliu.service.system;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** 单号生成：前缀 + 租户号(4) + 日期(8) + 序号(4)，按天从库内最大号续号，内存原子自增保证并发唯一。 */
@Service
@RequiredArgsConstructor
public class NumberService {
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Map<String, String[]> TABLES = Map.of(
            "YH", new String[]{"waybill", "waybill_no"},
            "PC", new String[]{"dispatch", "dispatch_no"},
            "DZ", new String[]{"statement", "statement_no"},
            "LP", new String[]{"claim", "claim_no"},
            "BJ", new String[]{"quote", "quote_no"},
            "ZD", new String[]{"bill", "bill_no"},
            "SK", new String[]{"payment", "payment_no"}
    );

    private final JdbcTemplate jdbcTemplate;
    private final Map<String, AtomicInteger> sequences = new ConcurrentHashMap<>();

    public String next(String prefix, Long tenantId) {
        String head = prefix + String.format("%04d", tenantId == null ? 0 : tenantId) + LocalDate.now().format(DAY);
        AtomicInteger seq = sequences.computeIfAbsent(head, this::loadMax);
        return head + String.format("%04d", seq.incrementAndGet());
    }

    private AtomicInteger loadMax(String head) {
        String prefix = head.substring(0, 2);
        String[] table = TABLES.get(prefix);
        if (table == null) return new AtomicInteger(0);
        try {
            String max = jdbcTemplate.queryForObject(
                    "SELECT MAX(" + table[1] + ") FROM " + table[0] + " WHERE " + table[1] + " LIKE ?",
                    String.class, head + "%");
            if (max != null && max.length() >= 4) {
                return new AtomicInteger(Integer.parseInt(max.substring(max.length() - 4)));
            }
        } catch (Exception ignored) {
        }
        return new AtomicInteger(0);
    }
}
