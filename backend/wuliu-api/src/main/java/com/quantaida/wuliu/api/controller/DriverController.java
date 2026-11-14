package com.quantaida.wuliu.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.wuliu.common.context.TenantContext;
import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.domain.entity.*;
import com.quantaida.wuliu.domain.mapper.*;
import com.quantaida.wuliu.service.sign.SignService;
import com.quantaida.wuliu.service.track.TrackService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/** 司机端（App/H5 兜底）与承运商端共用作业接口。 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DriverController {
    private final WaybillMapper waybillMapper;
    private final DispatchMapper dispatchMapper;
    private final PayableMapper payableMapper;
    private final StatementMapper statementMapper;
    private final TrackService trackService;
    private final SignService signService;

    @GetMapping("/driver/tasks")
    public List<Map<String, Object>> driverTasks(@RequestParam(required = false) String status) {
        Long driverId = TenantContext.driverId();
        if (driverId == null) throw BizException.forbidden("请先登录司机端");
        List<Waybill> list = waybillMapper.selectList(new LambdaQueryWrapper<Waybill>()
                .eq(Waybill::getDriverId, driverId)
                .eq(status != null && !status.isBlank(), Waybill::getStatus, status)
                .orderByAsc(Waybill::getPickupTime));
        return list.stream().map(this::toTask).toList();
    }

    private Map<String, Object> toTask(Waybill wb) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", wb.getId());
        m.put("waybillNo", wb.getWaybillNo());
        m.put("status", wb.getStatus());
        m.put("statusName", wb.getStatus());
        m.put("origin", wb.getOrigin());
        m.put("destination", wb.getDestination());
        m.put("senderName", wb.getSenderName());
        m.put("senderAddress", wb.getSenderAddress());
        m.put("receiverName", wb.getReceiverName());
        m.put("receiverAddress", wb.getReceiverAddress());
        m.put("receiverPhone", wb.getReceiverPhone());
        m.put("cargoName", wb.getCargoName());
        m.put("weight", wb.getWeight());
        m.put("volume", wb.getVolume());
        m.put("pieces", wb.getPieces());
        m.put("codAmount", wb.getCodAmount());
        m.put("plateNo", wb.getPlateNo());
        m.put("pickupTime", wb.getPickupTime());
        m.put("remark", wb.getRemark());
        return m;
    }

    @PostMapping("/driver/tasks/{id}/checkin")
    public Map<String, Object> checkin(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        trackService.checkin(id, dec(body.get("lat")), dec(body.get("lng")),
                body.get("address") == null ? null : String.valueOf(body.get("address")));
        return Map.of("ok", true, "status", "已提货");
    }

    @PostMapping("/driver/tasks/{id}/weight")
    public Map<String, Object> weight(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        List<String> photos = body.get("photos") instanceof List<?> l ? l.stream().map(String::valueOf).toList() : List.of();
        return trackService.weigh(id, dec(body.get("weight")), dec(body.get("volume")),
                body.get("pieces") == null ? null : Integer.parseInt(String.valueOf(body.get("pieces"))), photos);
    }

    @PostMapping("/driver/tasks/{id}/load")
    public Map<String, Object> load(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        List<String> photos = body.get("photos") instanceof List<?> l ? l.stream().map(String::valueOf).toList() : List.of();
        trackService.load(id, photos, List.of());
        return Map.of("ok", true, "status", "运输中");
    }

    @PostMapping("/driver/tasks/{id}/nodes")
    public Map<String, Object> node(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        trackService.reportNode(id, String.valueOf(body.getOrDefault("node", "已发车")),
                dec(body.get("lat")), dec(body.get("lng")),
                body.get("remark") == null ? null : String.valueOf(body.get("remark")));
        return Map.of("ok", true);
    }

    @PostMapping("/driver/tasks/{id}/sign")
    public Map<String, Object> sign(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        SignRecord record = signService.sign(id, String.valueOf(body.getOrDefault("signerName", "收货人")),
                body.get("signerPhone") == null ? null : String.valueOf(body.get("signerPhone")),
                String.valueOf(body.getOrDefault("signImage", "")),
                dec(body.get("lat")), dec(body.get("lng")));
        return Map.of("ok", true, "status", "已签收", "signedAt", record.getSignedAt());
    }

    @PostMapping("/driver/tasks/{id}/receipt")
    public Map<String, Object> receipt(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        List<String> images = body.get("images") instanceof List<?> l ? l.stream().map(String::valueOf).toList() : List.of();
        signService.uploadReceipt(id, images);
        return Map.of("ok", true);
    }

    @PostMapping("/driver/tasks/{id}/exception")
    public Map<String, Object> exception(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        List<String> images = body.get("images") instanceof List<?> l ? l.stream().map(String::valueOf).toList() : List.of();
        trackService.reportException(id, String.valueOf(body.getOrDefault("type", "延误")),
                body.get("description") == null ? null : String.valueOf(body.get("description")), images, false);
        return Map.of("ok", true);
    }

    @PostMapping("/driver/gps")
    public Map<String, Object> gps(@RequestBody Map<String, Object> body) {
        Long waybillId = body.get("waybillId") == null ? null : Long.valueOf(String.valueOf(body.get("waybillId")));
        Long vehicleId = body.get("vehicleId") == null ? null : Long.valueOf(String.valueOf(body.get("vehicleId")));
        if (waybillId == null) return Map.of("ok", true);
        trackService.gps(waybillId, vehicleId, dec(body.get("lat")), dec(body.get("lng")), dec(body.get("speed")));
        return Map.of("ok", true);
    }

    @GetMapping("/driver/income")
    public Map<String, Object> income() {
        Long driverId = TenantContext.driverId();
        List<Payable> list = payableMapper.selectList(new LambdaQueryWrapper<Payable>()
                .eq(Payable::getDriverId, driverId).orderByDesc(Payable::getId));
        BigDecimal total = list.stream().map(p -> p.getAmount() == null ? BigDecimal.ZERO : p.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("total", total);
        m.put("byOrder", list.stream().map(p -> Map.of("waybillNo", p.getWaybillNo(),
                "amount", p.getAmount(), "status", p.getStatus())).toList());
        return m;
    }

    @GetMapping("/carrier/orders")
    public List<Map<String, Object>> carrierOrders() {
        List<Waybill> list = waybillMapper.selectList(new LambdaQueryWrapper<Waybill>()
                .in(Waybill::getStatus, "已受理", "待调度").orderByAsc(Waybill::getPickupTime));
        return list.stream().map(w -> {
            Map<String, Object> m = toTask(w);
            m.put("receivable", w.getTotalAmount());
            m.put("payable", w.getPayableAmount());
            return m;
        }).toList();
    }

    @PostMapping("/carrier/orders/{id}/accept")
    public Map<String, Object> accept(@PathVariable Long id) {
        Waybill wb = waybillMapper.selectById(id);
        if (wb == null) throw BizException.notFound("运单不存在");
        Long carrierId = TenantContext.carrierId();
        if (carrierId == null) throw BizException.forbidden("请先登录承运商端");
        Dispatch dispatch = new Dispatch();
        dispatch.setWaybillId(id);
        dispatch.setCarrierId(carrierId);
        dispatchMapper.insert(dispatch);
        if ("已受理".equals(wb.getStatus())) {
            return Map.of("ok", true, "status", "待调度");
        }
        return Map.of("ok", true, "status", wb.getStatus());
    }

    @GetMapping("/carrier/tasks")
    public List<Map<String, Object>> carrierTasks(@RequestParam(required = false) String status) {
        Long carrierId = TenantContext.carrierId();
        List<Waybill> list = waybillMapper.selectList(new LambdaQueryWrapper<Waybill>()
                .eq(Waybill::getCarrierId, carrierId)
                .eq(status != null && !status.isBlank(), Waybill::getStatus, status)
                .orderByAsc(Waybill::getPickupTime));
        return list.stream().map(this::toTask).toList();
    }

    @PostMapping("/carrier/tasks/{id}/report")
    public Map<String, Object> carrierReport(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return node(id, body);
    }

    @GetMapping("/carrier/statements")
    public List<Map<String, Object>> carrierStatements() {
        Long carrierId = TenantContext.carrierId();
        List<Payable> payables = payableMapper.selectList(new LambdaQueryWrapper<Payable>()
                .eq(Payable::getCarrierId, carrierId).orderByDesc(Payable::getId));
        return payables.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("waybillNo", p.getWaybillNo());
            m.put("amount", p.getAmount());
            m.put("status", p.getStatus());
            return m;
        }).toList();
    }

    @PostMapping("/carrier/statements/{id}/confirm")
    public Map<String, Object> carrierConfirm(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Payable p = payableMapper.selectById(id);
        if (p == null) throw BizException.notFound("应付单不存在");
        p.setStatus(Boolean.parseBoolean(String.valueOf(body.getOrDefault("pass", "true"))) ? "客户已确认" : "有异议");
        payableMapper.updateById(p);
        return Map.of("ok", true);
    }

    @GetMapping("/h5/receipt/{waybillNo}")
    public Map<String, Object> h5Receipt(@PathVariable String waybillNo, @RequestParam(required = false) String sign) {
        return signService.h5Receipt(waybillNo, sign);
    }

    @PostMapping("/h5/quote/share")
    public Map<String, Object> shareQuote(@RequestBody Map<String, Object> body) {
        return Map.of("shareUrl", "https://www.quantaida.cloud/h5/quote-share?q=" + body.getOrDefault("quoteNo", ""));
    }

    private static BigDecimal dec(Object v) {
        if (v == null) return null;
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
