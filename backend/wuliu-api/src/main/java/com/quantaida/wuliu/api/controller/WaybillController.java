package com.quantaida.wuliu.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quantaida.wuliu.common.context.TenantContext;
import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.common.result.PageResult;
import com.quantaida.wuliu.common.security.RateLimiter;
import com.quantaida.wuliu.domain.entity.Waybill;
import com.quantaida.wuliu.domain.entity.WaybillItem;
import com.quantaida.wuliu.domain.mapper.WaybillMapper;
import com.quantaida.wuliu.service.order.WaybillService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/** 运单：列表、详情、新建、改单、状态机动作、打印。 */
@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
public class WaybillController {
    private final WaybillMapper waybillMapper;
    private final WaybillService waybillService;
    private final RateLimiter rateLimiter;

    @GetMapping("/waybills")
    public PageResult<Waybill> waybills(@RequestParam Map<String, Object> params) {
        var qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Waybill>();
        String status = params.get("status") == null ? "" : String.valueOf(params.get("status"));
        if (!status.isBlank()) qw.eq("status", status);
        String keyword = params.get("keyword") == null ? "" : String.valueOf(params.get("keyword"));
        if (!keyword.isBlank()) qw.and(w -> w.like("waybill_no", keyword)
                .or().like("customer_name", keyword).or().like("plate_no", keyword));
        for (String key : List.of("customerId", "settleType", "origin", "destination", "driverName", "vehicleType")) {
            if (params.get(key) != null && !String.valueOf(params.get(key)).isBlank()) {
                qw.eq(camel(key), params.get(key));
            }
        }
        if (params.get("startDate") != null && !String.valueOf(params.get("startDate")).isBlank()) {
            qw.ge("created_at", params.get("startDate"));
        }
        if (params.get("endDate") != null && !String.valueOf(params.get("endDate")).isBlank()) {
            qw.le("created_at", String.valueOf(params.get("endDate")) + " 23:59:59");
        }
        qw.orderByDesc("id");
        Page<Waybill> page = waybillMapper.selectPage(new Page<>(parseLong(params.get("page"), 1),
                parseLong(params.get("size"), 10)), qw);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @PostMapping("/waybills")
    public Map<String, Object> create(@RequestBody Map<String, Object> body) {
        rateLimiter.acquire("waybill:create:" + TenantContext.userId(), 10, 60);
        Waybill wb = new com.fasterxml.jackson.databind.ObjectMapper().convertValue(body, Waybill.class);
        List<WaybillItem> items = new ArrayList<>();
        Object rawItems = body.get("items");
        if (rawItems instanceof List<?> list) {
            items = new com.fasterxml.jackson.databind.ObjectMapper()
                    .convertValue(list, new com.fasterxml.jackson.core.type.TypeReference<List<WaybillItem>>() {
                    });
        }
        List<String> photos = new ArrayList<>();
        if (body.get("photos") instanceof List<?> list) photos = list.stream().map(String::valueOf).toList();
        return waybillService.create(wb, items, photos);
    }

    @GetMapping("/waybills/{id}")
    public Waybill detail(@PathVariable Long id) {
        Waybill wb = waybillMapper.selectById(id);
        if (wb == null) throw BizException.notFound("运单不存在");
        return wb;
    }

    @GetMapping("/waybills/{id}/timeline")
    public Map<String, Object> timeline(@PathVariable Long id) {
        return waybillService.timeline(id);
    }

    @PutMapping("/waybills/{id}")
    public Waybill update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Waybill patch = new com.fasterxml.jackson.databind.ObjectMapper().convertValue(body, Waybill.class);
        return waybillService.update(id, patch, String.valueOf(body.getOrDefault("changeReason", "改单")));
    }

    @PostMapping("/waybills/{id}/action")
    public Map<String, Object> action(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String action = String.valueOf(body.getOrDefault("action", ""));
        Waybill wb = waybillService.transition(id, action,
                body.get("reason") == null ? null : String.valueOf(body.get("reason")), body);
        return Map.of("id", id, "status", wb.getStatus(), "statusName", wb.getStatus(), "waybillNo", wb.getWaybillNo());
    }

    @GetMapping("/waybills/{id}/print")
    public String print(@PathVariable Long id) {
        Waybill wb = waybillMapper.selectById(id);
        if (wb == null) throw BizException.notFound("运单不存在");
        return """
                <html><head><meta charset="utf-8"><title>托运单</title><style>body{font-family:'Microsoft YaHei';padding:24px}
                h2{text-align:center;color:#1f6feb}h1{text-align:center;font-size:22px}
                table{width:100%;border-collapse:collapse;margin-top:16px}td,th{border:1px solid #ccc;padding:8px;font-size:14px}
                .big{font-size:18px;color:#c00}</style></head><body>
                <h1>托运单</h1><p style="text-align:right">运单号：<b>%s</b></p>
                <table><tr><td>发货人</td><td>%s</td><td>联系电话</td><td>%s</td></tr>
                <tr><td>收货人</td><td>%s</td><td>联系电话</td><td>%s</td></tr>
                <tr><td>出发地</td><td>%s</td><td>目的地</td><td>%s</td></tr>
                <tr><td>货物名称</td><td>%s</td><td>数量/件数</td><td>%s</td></tr>
                <tr><td>重量(kg)</td><td>%s</td><td>体积(m³)</td><td>%s</td></tr>
                <tr><td>车型</td><td>%s</td><td>运输方式</td><td>%s</td></tr>
                <tr><td>费用合计</td><td class="big">%s 元</td><td>状态</td><td>%s</td></tr></table>
                <p style="margin-top:24px;font-size:12px;color:#888">本托运单为演示模板，正式打印可自定义抬头与条款。</p>
                </body></html>""".formatted(
                wb.getWaybillNo(), wb.getSenderName(), wb.getSenderPhone(), wb.getReceiverName(), wb.getReceiverPhone(),
                wb.getOrigin(), wb.getDestination(), wb.getCargoName(), wb.getPieces(),
                wb.getWeight(), wb.getVolume(), wb.getVehicleType(),
                ("上门提货".equals(wb.getPickupType()) ? "上门提货" : "自送到站"),
                wb.getTotalAmount(), wb.getStatus());
    }

    @GetMapping("/waybills/{id}/label")
    public String label(@PathVariable Long id) {
        Waybill wb = waybillMapper.selectById(id);
        if (wb == null) throw BizException.notFound("运单不存在");
        return """
                <html><head><meta charset="utf-8"><title>标签</title><style>body{font-family:'Microsoft YaHei';margin:0}
                .tag{width:100mm;height:150mm;border:2px solid #333;padding:4mm;box-sizing:border-box}
                .no{font-size:13px;word-break:break-all}.dest{font-size:26px;font-weight:bold;margin:8mm 0}
                .route{font-size:16px}.barcode{height:14mm;margin-top:6mm;background:repeating-linear-gradient(90deg,#000 0 1mm,#fff 1mm 2mm)}</style></head><body>
                <div class="tag"><div class="no">运单号：%s</div>
                <div class="dest">→ %s</div><div class="route">%s → %s</div>
                <div>收货人：%s　%s</div><div>货物：%s　%skg</div>
                <div class="barcode"></div></div></body></html>""".formatted(
                wb.getWaybillNo(), wb.getDestination(), wb.getOrigin(), wb.getDestination(),
                wb.getReceiverName(), wb.getReceiverPhone(), wb.getCargoName(), wb.getWeight());
    }

    @PostMapping("/waybills/import")
    public Map<String, Object> importWaybills(@RequestBody List<Map<String, Object>> rows) {
        int ok = 0;
        List<String> errors = new ArrayList<>();
        int i = 0;
        for (Map<String, Object> row : rows) {
            i++;
            try {
                Waybill wb = new com.fasterxml.jackson.databind.ObjectMapper().convertValue(row, Waybill.class);
                waybillService.create(wb, List.of(), List.of());
                ok++;
            } catch (Exception e) {
                errors.add("第 " + i + " 行：" + e.getMessage());
            }
        }
        return Map.of("success", ok, "failed", errors.size(), "errors", errors);
    }

    private static long parseLong(Object v, long def) {
        try {
            return v == null ? def : Long.parseLong(String.valueOf(v));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static String camel(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isUpperCase(c)) sb.append('_').append(Character.toLowerCase(c));
            else sb.append(c);
        }
        return sb.toString();
    }
}
