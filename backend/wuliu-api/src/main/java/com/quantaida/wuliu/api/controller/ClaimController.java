package com.quantaida.wuliu.api.controller;

import com.quantaida.wuliu.common.result.PageResult;
import com.quantaida.wuliu.domain.entity.Claim;
import com.quantaida.wuliu.domain.entity.Complaint;
import com.quantaida.wuliu.service.claim.ClaimService;
import com.quantaida.wuliu.service.report.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/** 理赔、投诉、报表。 */
@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
public class ClaimController {
    private final ClaimService claimService;
    private final ReportService reportService;

    @GetMapping("/claims")
    public PageResult<Claim> claims(@RequestParam Map<String, Object> params) {
        List<Claim> list = claimService.list();
        return PageResult.of(list, list.size(), 1, list.isEmpty() ? 10 : list.size());
    }

    @PostMapping("/claims")
    public Claim create(@RequestBody Map<String, Object> body) {
        List<String> evidence = body.get("evidence") instanceof List<?> l ? l.stream().map(String::valueOf).toList() : List.of();
        return claimService.create(Long.valueOf(String.valueOf(body.get("waybillId"))),
                String.valueOf(body.getOrDefault("recordType", "破损")),
                String.valueOf(body.getOrDefault("description", "")), evidence);
    }

    @PostMapping("/claims/{id}/liability")
    public Map<String, Object> liability(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        claimService.liability(id, String.valueOf(body.getOrDefault("liability", "待定")),
                String.valueOf(body.getOrDefault("reason", "")));
        return Map.of("done", true);
    }

    @PostMapping("/claims/{id}/approve")
    public Map<String, Object> approve(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        BigDecimal amount = body.get("amount") == null ? null : new BigDecimal(String.valueOf(body.get("amount")));
        claimService.approve(id, Boolean.parseBoolean(String.valueOf(body.getOrDefault("pass", "true"))),
                amount, String.valueOf(body.getOrDefault("reason", "")));
        return Map.of("done", true);
    }

    @PostMapping("/claims/{id}/pay")
    public Map<String, Object> pay(@PathVariable Long id) {
        claimService.approve(id, true,
                claimService.list().stream().filter(c -> c.getId().equals(id)).findFirst().orElseThrow().getAmount(),
                "赔付登记");
        return Map.of("paid", true);
    }

    @PostMapping("/claims/{id}/close")
    public Map<String, Object> close(@PathVariable Long id) {
        claimService.close(id);
        return Map.of("closed", true);
    }

    @GetMapping("/complaints")
    public List<Complaint> complaints() {
        return claimService.complaints();
    }

    @PostMapping("/complaints")
    public Complaint createComplaint(@RequestBody Complaint complaint) {
        return claimService.createComplaint(complaint);
    }

    @GetMapping("/reports/{type}")
    public Map<String, Object> report(@PathVariable String type) {
        return reportService.report(type);
    }

    @GetMapping("/reports/{type}/export")
    public Map<String, Object> export(@PathVariable String type, @RequestParam String purpose) {
        return reportService.export(type, purpose);
    }
}
