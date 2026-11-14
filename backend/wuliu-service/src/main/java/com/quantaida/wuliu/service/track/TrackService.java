package com.quantaida.wuliu.service.track;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.wuliu.common.context.TenantContext;
import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.domain.entity.*;
import com.quantaida.wuliu.domain.mapper.*;
import com.quantaida.wuliu.service.message.MessageService;
import com.quantaida.wuliu.service.order.WaybillService;
import com.quantaida.wuliu.service.pricing.PriceRequest;
import com.quantaida.wuliu.service.pricing.PricingService;
import com.quantaida.wuliu.service.system.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/** 在途：签到、称重差异改价、装车、节点上报、GPS 轨迹、异常单。 */
@Service
@RequiredArgsConstructor
public class TrackService {
    private final WaybillMapper waybillMapper;
    private final VehicleMapper vehicleMapper;
    private final DispatchMapper dispatchMapper;
    private final TrackPointMapper pointMapper;
    private final TrackNodeMapper nodeMapper;
    private final ExceptionOrderMapper exceptionMapper;
    private final WaybillPhotoMapper photoMapper;
    private final WaybillService waybillService;
    private final PricingService pricingService;
    private final MessageService messageService;
    private final AuditService auditService;

    @Transactional
    public void checkin(Long waybillId, BigDecimal lat, BigDecimal lng, String address) {
        Waybill wb = require(waybillId);
        if (!"已派车".equals(wb.getStatus())) {
            throw new BizException("运单当前状态为「" + wb.getStatus() + "」，不能签到");
        }
        Dispatch dispatch = dispatchMapper.selectOne(new LambdaQueryWrapper<Dispatch>()
                .eq(Dispatch::getWaybillId, waybillId).orderByDesc(Dispatch::getId).last("limit 1"));
        if (dispatch != null) {
            dispatch.setAcceptedAt(LocalDateTime.now());
            dispatch.setStatus("已接单");
            dispatchMapper.updateById(dispatch);
        }
        waybillService.transition(waybillId, "pickup", "司机到场签到：" + (address == null ? "" : address), null);
        saveNode(waybillId, "到达发货地", lat, lng, "签到");
        auditService.log("到场签到", wb.getWaybillNo(), address);
    }

    /** 称重量方：与申报差异 > 5% 生成改价单并阻断进入运输中。 */
    @Transactional
    public Map<String, Object> weigh(Long waybillId, BigDecimal weight, BigDecimal volume, Integer pieces, List<String> photos) {
        Waybill wb = require(waybillId);
        if (!"已提货".equals(wb.getStatus()) && !"已派车".equals(wb.getStatus())) {
            throw new BizException("当前状态不能称重量方");
        }
        if (weight == null || weight.signum() <= 0) throw new BizException("请录入实际重量");
        BigDecimal declared = wb.getWeight() == null ? BigDecimal.ONE : wb.getWeight();
        BigDecimal diff = weight.subtract(declared).abs().divide(declared, 4, RoundingMode.HALF_UP);
        wb.setActualWeight(weight);
        wb.setActualVolume(volume);
        if (pieces != null) wb.setPieces(pieces);
        boolean over = diff.compareTo(new BigDecimal("0.05")) > 0;
        if (over) {
            wb.setRepriceStatus("待客户确认");
            PriceRequest req = new PriceRequest();
            req.setCustomerId(wb.getCustomerId());
            req.setOrigin(wb.getOrigin());
            req.setDestination(wb.getDestination());
            req.setWeight(weight);
            req.setVolume(volume == null ? BigDecimal.ZERO : volume);
            req.setVehicleType(wb.getVehicleType());
            req.setCodAmount(wb.getCodAmount());
            Map<String, Object> price = pricingService.quote(req, TenantContext.requireTenantId(), true);
            BigDecimal newTotal = new BigDecimal(String.valueOf(price.get("totalAmount")));
            wb.setRepriceAmount(newTotal);
            messageService.notify("改价确认", wb.getId(), wb.getSenderPhone(), wb.getWaybillNo(),
                    "申报 " + declared + "kg，实际 " + weight + "kg，新价 " + newTotal + " 元");
        }
        waybillMapper.updateById(wb);
        if (photos != null) for (String url : photos) savePhoto(waybillId, "称重", url);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("declaredWeight", declared);
        result.put("actualWeight", weight);
        result.put("variance", diff.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));
        result.put("repriceRequired", over);
        result.put("repriceAmount", wb.getRepriceAmount());
        result.put("status", wb.getStatus());
        auditService.log("称重量方", wb.getWaybillNo(), "申报 " + declared + "kg，实际 " + weight + "kg，差异 " + result.get("variance") + "%");
        return result;
    }

    @Transactional
    public void confirmReprice(Long waybillId, boolean pass) {
        Waybill wb = require(waybillId);
        if (!"待客户确认".equals(wb.getRepriceStatus())) throw new BizException("该运单无待确认的改价单");
        wb.setRepriceStatus(pass ? "已确认" : "未确认");
        if (pass && wb.getRepriceAmount() != null) {
            wb.setTotalAmount(wb.getRepriceAmount());
            wb.setReceivableAmount(wb.getRepriceAmount());
            wb.setGrossProfit(PricingService.money(wb.getTotalAmount().subtract(
                    wb.getPayableAmount() == null ? BigDecimal.ZERO : wb.getPayableAmount())));
        }
        waybillMapper.updateById(wb);
        auditService.log("改价确认", wb.getWaybillNo(), pass ? "客户确认新价" : "客户未确认，按申报价结算差额挂账");
    }

    @Transactional
    public void load(Long waybillId, List<String> photos, List<Map<String, Object>> items) {
        Waybill wb = require(waybillId);
        if (!"已提货".equals(wb.getStatus())) throw new BizException("只有已提货运单可以装车");
        if (wb.getActualWeight() == null) throw new BizException("未完成称重量方，不能装车");
        if ("待客户确认".equals(wb.getRepriceStatus())) {
            throw new BizException("称重差异待客户确认，暂不能进入运输中");
        }
        if (photos != null) for (String url : photos) savePhoto(waybillId, "装车", url);
        saveNode(waybillId, "装车完成", null, null, null);
        waybillService.transition(waybillId, "ship", "装车完成", null);
    }

    @Transactional
    public void reportNode(Long waybillId, String node, BigDecimal lat, BigDecimal lng, String remark) {
        Waybill wb = require(waybillId);
        String action = switch (node) {
            case "已发车" -> "ship";
            case "中转到达" -> "transfer";
            case "中转发出" -> "depart";
            case "到达目的站" -> "arrive";
            case "派送中" -> "deliver";
            default -> null;
        };
        saveNode(waybillId, node, lat, lng, remark);
        if (action != null) {
            try {
                waybillService.transition(waybillId, action, node, null);
            } catch (BizException e) {
                // 节点可能重复上报，状态机拦截时只保留节点记录
                if (!e.getMessage().contains("不能执行该操作")) throw e;
            }
        }
        auditService.log("节点上报", wb.getWaybillNo(), node);
    }

    @Transactional
    public void gps(Long waybillId, Long vehicleId, BigDecimal lat, BigDecimal lng, BigDecimal speed) {
        if (lat == null || lng == null) throw new BizException("缺少定位坐标");
        TrackPoint point = new TrackPoint();
        point.setWaybillId(waybillId);
        point.setVehicleId(vehicleId);
        point.setLat(lat);
        point.setLng(lng);
        point.setSpeed(speed);
        point.setReportedAt(LocalDateTime.now());
        point.setBucket(LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
        pointMapper.insert(point);
        if (vehicleId != null) {
            Vehicle vehicle = vehicleMapper.selectById(vehicleId);
            if (vehicle != null) {
                vehicle.setCurrentLat(lat);
                vehicle.setCurrentLng(lng);
                vehicleMapper.updateById(vehicle);
            }
        }
    }

    public Map<String, Object> track(Long waybillId) {
        Waybill wb = require(waybillId);
        List<TrackPoint> points = pointMapper.selectList(new LambdaQueryWrapper<TrackPoint>()
                .eq(TrackPoint::getWaybillId, waybillId).orderByAsc(TrackPoint::getReportedAt));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("waybillNo", wb.getWaybillNo());
        m.put("status", wb.getStatus());
        m.put("plateNo", wb.getPlateNo());
        m.put("points", points);
        m.put("nodes", nodeMapper.selectList(new LambdaQueryWrapper<TrackNode>()
                .eq(TrackNode::getWaybillId, waybillId).orderByAsc(TrackNode::getId)));
        m.put("latest", points.isEmpty() ? null : points.get(points.size() - 1));
        m.put("exceptions", exceptionMapper.selectList(new LambdaQueryWrapper<ExceptionOrder>()
                .eq(ExceptionOrder::getWaybillId, waybillId).orderByDesc(ExceptionOrder::getId)));
        return m;
    }

    public List<Map<String, Object>> replay(Long waybillId) {
        List<TrackPoint> points = pointMapper.selectList(new LambdaQueryWrapper<TrackPoint>()
                .eq(TrackPoint::getWaybillId, waybillId).orderByAsc(TrackPoint::getReportedAt));
        Map<String, Map<String, Object>> byMinute = new LinkedHashMap<>();
        for (TrackPoint p : points) {
            String key = p.getBucket() == null ? "" : p.getBucket();
            byMinute.putIfAbsent(key, new LinkedHashMap<>(Map.of(
                    "time", p.getReportedAt(), "lat", p.getLat(), "lng", p.getLng(), "speed", p.getSpeed())));
        }
        return new ArrayList<>(byMinute.values());
    }

    @Transactional
    public ExceptionOrder reportException(Long waybillId, String type, String description, List<String> images, boolean escalate) {
        Waybill wb = require(waybillId);
        if (type == null || type.isBlank()) throw new BizException("请选择异常类型");
        if ((description == null || description.isBlank()) && (images == null || images.isEmpty())) {
            throw new BizException("请填写异常描述或上传照片");
        }
        ExceptionOrder order = new ExceptionOrder();
        order.setWaybillId(waybillId);
        order.setRecordType(type);
        order.setDescription(description);
        try {
            order.setImages(images == null ? "[]" : new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(images));
        } catch (Exception ignored) {
            order.setImages("[]");
        }
        order.setStatus("待处理");
        order.setEscalated(escalate ? 1 : 0);
        exceptionMapper.insert(order);
        try {
            waybillService.transition(waybillId, "exception", type + "：" + description, null);
        } catch (BizException ignored) {
            // 已处于异常处理中时保持原状态
        }
        messageService.notify("异常通知", waybillId, wb.getSenderPhone(), wb.getWaybillNo(), type);
        auditService.log("异常上报", wb.getWaybillNo(), type + "：" + description);
        return order;
    }

    @Transactional
    public void handleException(Long id, String conclusion, String assignee, boolean escalate, String previousStatus) {
        ExceptionOrder order = exceptionMapper.selectById(id);
        if (order == null) throw BizException.notFound("异常单不存在");
        order.setStatus("已处理");
        order.setConclusion(conclusion);
        order.setAssignee(assignee);
        order.setEscalated(escalate ? 1 : 0);
        order.setHandledAt(LocalDateTime.now());
        exceptionMapper.updateById(order);
        Map<String, Object> payload = new HashMap<>();
        payload.put("previousStatus", previousStatus);
        try {
            waybillService.transition(order.getWaybillId(), "resolve", conclusion, payload);
        } catch (BizException ignored) {
            // 原状态不可恢复时保留异常处理记录
        }
        auditService.log("异常处置", String.valueOf(order.getWaybillId()), conclusion);
    }

    public List<Waybill> stagnant() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        return waybillMapper.selectList(new LambdaQueryWrapper<Waybill>()
                .in(Waybill::getStatus, "已受理", "待调度", "已派车", "运输中", "到达待派送", "派送中")
                .lt(Waybill::getUpdatedAt, threshold).orderByAsc(Waybill::getUpdatedAt));
    }

    private void saveNode(Long waybillId, String node, BigDecimal lat, BigDecimal lng, String remark) {
        TrackNode n = new TrackNode();
        n.setWaybillId(waybillId);
        n.setNode(node);
        n.setLat(lat);
        n.setLng(lng);
        n.setOperator(TenantContext.username());
        n.setRemark(remark);
        nodeMapper.insert(n);
    }

    private void savePhoto(Long waybillId, String stage, String url) {
        WaybillPhoto p = new WaybillPhoto();
        p.setWaybillId(waybillId);
        p.setStage(stage);
        p.setUrl(url);
        p.setSortNo(0);
        photoMapper.insert(p);
    }

    private Waybill require(Long waybillId) {
        Waybill wb = waybillMapper.selectById(waybillId);
        if (wb == null) throw BizException.notFound("运单不存在");
        return wb;
    }
}
