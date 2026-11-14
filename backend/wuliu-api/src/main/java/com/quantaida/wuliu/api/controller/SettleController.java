package com.quantaida.wuliu.api.controller;

import com.quantaida.wuliu.common.result.PageResult;
import com.quantaida.wuliu.domain.entity.Invoice;
import com.quantaida.wuliu.domain.entity.Payment;
import com.quantaida.wuliu.domain.entity.Payable;
import com.quantaida.wuliu.domain.entity.Receivable;
import com.quantaida.wuliu.domain.entity.Statement;
import com.quantaida.wuliu.service.settle.ConsistencyService;
import com.quantaida.wuliu.service.settle.SettleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/** 结算、对账、开票、收款、代收、毛利、一致性校对。 */
@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
public class SettleController {
    private final SettleService settleService;
    private final ConsistencyService consistencyService;

    @GetMapping("/receivables")
    public PageResult<Receivable> receivables(@RequestParam Map<String, Object> params) {
        List<Receivable> list = settleService.receivables();
        return page(list, params);
    }

    @GetMapping("/payables")
    public PageResult<Payable> payables(@RequestParam Map<String, Object> params) {
        List<Payable> list = settleService.payables();
        return page(list, params);
    }

    @GetMapping("/statements")
    public PageResult<Statement> statements(@RequestParam Map<String, Object> params) {
        List<Statement> list = settleService.statements();
        return page(list, params);
    }

    @PostMapping("/statements")
    public Statement generate(@RequestBody Map<String, Object> body) {
        String type = String.valueOf(body.getOrDefault("type", "客户"));
        Long partyId = body.get("partyId") == null ? null : Long.valueOf(String.valueOf(body.get("partyId")));
        LocalDateTime start = parseDate(body.get("periodStart"));
        LocalDateTime end = parseDate(body.get("periodEnd"));
        return settleService.generateStatement(type, partyId, start, end);
    }

    @GetMapping("/statements/{id}")
    public Map<String, Object> statementDetail(@PathVariable Long id) {
        return settleService.statementDetail(id);
    }

    @PostMapping("/statements/{id}/confirm")
    public Map<String, Object> confirm(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        settleService.confirm(id, Boolean.parseBoolean(String.valueOf(body.getOrDefault("pass", "true"))),
                body.get("reason") == null ? "" : String.valueOf(body.get("reason")));
        return Map.of("confirmed", true);
    }

    @PostMapping("/statements/{id}/dispute")
    public Map<String, Object> dispute(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        settleService.confirm(id, false, String.valueOf(body.getOrDefault("reason", "未填写异议原因")));
        return Map.of("disputed", true);
    }

    @GetMapping("/statements/{id}/pdf")
    public Map<String, Object> statementPdf(@PathVariable Long id) {
        Map<String, Object> detail = settleService.statementDetail(id);
        return Map.of("downloadUrl", "/api/files/demo/statement-" + id + ".pdf", "statement", detail.get("statement"));
    }

    @GetMapping("/invoices")
    public PageResult<Invoice> invoices(@RequestParam Map<String, Object> params) {
        List<Invoice> list = settleService.invoices();
        return page(list, params);
    }

    @PostMapping("/invoices")
    public Invoice invoice(@RequestBody Map<String, Object> body) {
        return settleService.invoice(Long.valueOf(String.valueOf(body.get("statementId"))),
                String.valueOf(body.getOrDefault("invoiceNo", "INV" + System.currentTimeMillis() % 100000)),
                String.valueOf(body.getOrDefault("invoiceType", "增值税普通发票")),
                body.get("amount") == null ? null : new BigDecimal(String.valueOf(body.get("amount"))));
    }

    @GetMapping("/payments")
    public PageResult<Payment> payments(@RequestParam Map<String, Object> params) {
        List<Payment> list = settleService.payments();
        return page(list, params);
    }

    @PostMapping("/payments")
    public Payment payment(@RequestBody Map<String, Object> body) {
        return settleService.payment(Long.valueOf(String.valueOf(body.get("statementId"))),
                new BigDecimal(String.valueOf(body.get("amount"))),
                String.valueOf(body.getOrDefault("method", "银行转账")),
                body.get("differenceReason") == null ? null : String.valueOf(body.get("differenceReason")));
    }

    @GetMapping("/aging")
    public Map<String, Object> aging() {
        return settleService.aging();
    }

    @GetMapping("/cod")
    public Map<String, Object> cod(@RequestParam Map<String, Object> params) {
        return Map.of("list", settleService.codList());
    }

    @PostMapping("/cod/{id}/settle")
    public Map<String, Object> codSettle(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        settleService.codSettle(id, String.valueOf(body.getOrDefault("channelNo", "")));
        return Map.of("settled", true);
    }

    @GetMapping("/profit")
    public Map<String, Object> profit() {
        return settleService.profit();
    }

    @PostMapping("/consistency/check")
    public Map<String, Object> consistencyCheck() {
        return consistencyService.check();
    }

    @GetMapping("/consistency/tasks")
    public List<com.quantaida.wuliu.domain.entity.RepairTask> consistencyTasks() {
        return consistencyService.tasks();
    }

    @PostMapping("/consistency/tasks/{id}/handle")
    public Map<String, Object> handleTask(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        consistencyService.handle(id, String.valueOf(body.getOrDefault("detail", "已修复")));
        return Map.of("handled", true);
    }

    private <T> PageResult<T> page(List<T> list, Map<String, Object> params) {
        return PageResult.of(list, list.size(), 1, list.isEmpty() ? 10 : list.size());
    }

    private static LocalDateTime parseDate(Object v) {
        if (v == null) return null;
        try {
            return LocalDateTime.parse(String.valueOf(v).replace(" ", "T"));
        } catch (Exception e) {
            return null;
        }
    }
}
