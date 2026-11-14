package com.quantaida.wuliu.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quantaida.wuliu.common.result.PageResult;
import com.quantaida.wuliu.domain.entity.ExceptionOrder;
import com.quantaida.wuliu.domain.entity.ReturnTask;
import com.quantaida.wuliu.domain.mapper.ExceptionOrderMapper;
import com.quantaida.wuliu.domain.mapper.ReturnTaskMapper;
import com.quantaida.wuliu.service.sign.SignService;
import com.quantaida.wuliu.service.track.TimeoutService;
import com.quantaida.wuliu.service.track.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/** 在途、异常、回单、退回、卡滞看板。 */
@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
public class TrackController {
    private final TrackService trackService;
    private final SignService signService;
    private final TimeoutService timeoutService;
    private final ExceptionOrderMapper exceptionMapper;
    private final ReturnTaskMapper returnTaskMapper;

    @GetMapping("/tracks/{waybillId}")
    public Map<String, Object> track(@PathVariable Long waybillId) {
        return trackService.track(waybillId);
    }

    @GetMapping("/tracks/{waybillId}/replay")
    public List<Map<String, Object>> replay(@PathVariable Long waybillId) {
        return trackService.replay(waybillId);
    }

    @GetMapping("/exceptions")
    public PageResult<ExceptionOrder> exceptions(@RequestParam Map<String, Object> params) {
        var qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ExceptionOrder>();
        String status = params.get("status") == null ? "" : String.valueOf(params.get("status"));
        if (!status.isBlank()) qw.eq("status", status);
        String type = params.get("recordType") == null ? "" : String.valueOf(params.get("recordType"));
        if (!type.isBlank()) qw.eq("record_type", type);
        qw.orderByDesc("id");
        var page = exceptionMapper.selectPage(new Page<>(parseLong(params.get("page"), 1),
                parseLong(params.get("size"), 10)), qw);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @PostMapping("/exceptions")
    public ExceptionOrder report(@RequestBody Map<String, Object> body) {
        Long waybillId = Long.valueOf(String.valueOf(body.get("waybillId")));
        List<String> images = body.get("images") instanceof List<?> l ? l.stream().map(String::valueOf).toList() : List.of();
        return trackService.reportException(waybillId, String.valueOf(body.getOrDefault("recordType", "延误")),
                body.get("description") == null ? null : String.valueOf(body.get("description")), images,
                Boolean.parseBoolean(String.valueOf(body.getOrDefault("escalate", "false"))));
    }

    @PostMapping("/exceptions/{id}/handle")
    public Map<String, Object> handle(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        trackService.handleException(id, String.valueOf(body.getOrDefault("conclusion", "")),
                body.get("assignee") == null ? null : String.valueOf(body.get("assignee")),
                Boolean.parseBoolean(String.valueOf(body.getOrDefault("escalate", "false"))),
                body.get("previousStatus") == null ? null : String.valueOf(body.get("previousStatus")));
        return Map.of("handled", true);
    }

    @GetMapping("/receipts")
    public PageResult<com.quantaida.wuliu.domain.entity.Receipt> receipts(@RequestParam Map<String, Object> params) {
        String status = params.get("status") == null ? "" : String.valueOf(params.get("status"));
        var list = signService.receiptList(status);
        return PageResult.of(list, list.size(), 1, list.size());
    }

    @PostMapping("/receipts/{id}/review")
    public Map<String, Object> review(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        signService.reviewReceipt(id, Boolean.parseBoolean(String.valueOf(body.getOrDefault("pass", "false"))),
                body.get("reason") == null ? "" : String.valueOf(body.get("reason")));
        return Map.of("reviewed", true);
    }

    @GetMapping("/returns")
    public PageResult<ReturnTask> returns(@RequestParam Map<String, Object> params) {
        List<ReturnTask> list = signService.returns();
        return PageResult.of(list, list.size(), 1, list.size());
    }

    @PostMapping("/returns/{id}/complete")
    public Map<String, Object> completeReturn(@PathVariable Long id) {
        signService.completeReturn(id);
        return Map.of("completed", true);
    }

    @GetMapping("/stagnant")
    public Map<String, Object> stagnant() {
        return Map.of("list", trackService.stagnant(), "count", trackService.stagnant().size());
    }

    @PostMapping("/timeouts/scan")
    public Map<String, Object> scanTimeouts() {
        return timeoutService.scan();
    }

    private static long parseLong(Object v, long def) {
        try {
            return v == null ? def : Long.parseLong(String.valueOf(v));
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
