package com.quantaida.wuliu.service.dispatch;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.wuliu.common.context.TenantContext;
import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.domain.entity.*;
import com.quantaida.wuliu.domain.mapper.*;
import com.quantaida.wuliu.service.message.MessageService;
import com.quantaida.wuliu.service.order.WaybillService;
import com.quantaida.wuliu.service.system.AuditService;
import com.quantaida.wuliu.service.system.NumberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** 调度派车：资质前置校验、自有车/外协匹配、派车单生成、改派、空驶费登记、看板数据。 */
@Service
@RequiredArgsConstructor
public class DispatchService {
    private final DispatchMapper dispatchMapper;
    private final WaybillMapper waybillMapper;
    private final VehicleMapper vehicleMapper;
    private final DriverMapper driverMapper;
    private final CarrierMapper carrierMapper;
    private final WaybillService waybillService;
    private final NumberService numberService;
    private final MessageService messageService;
    private final AuditService auditService;

    @Transactional
    public Map<String, Object> assign(Long waybillId, Long vehicleId, Long driverId, Long carrierId, String remark) {
        Waybill wb = waybillMapper.selectById(waybillId);
        if (wb == null) throw BizException.notFound("运单不存在");
        if (!List.of("已受理", "待调度").contains(wb.getStatus())) {
            throw new BizException("运单当前状态为「" + wb.getStatus() + "」，只有已受理/待调度可以派车");
        }
        Vehicle vehicle = null;
        Driver driver = null;
        Carrier carrier = null;
        if (vehicleId != null) {
            vehicle = vehicleMapper.selectById(vehicleId);
            if (vehicle == null) throw new BizException("车辆不存在");
            assertValid(vehicle.getTransportExpire(), "车辆 " + vehicle.getPlateNo() + " 的《道路运输证》");
        }
        if (driverId != null) {
            driver = driverMapper.selectById(driverId);
            if (driver == null) throw new BizException("司机不存在");
            assertValid(driver.getLicenseExpire(), "司机 " + driver.getName() + " 的驾驶证");
            assertValid(driver.getQualificationExpire(), "司机 " + driver.getName() + " 的《道路运输从业人员从业资格证》");
        }
        if (carrierId != null) {
            carrier = carrierMapper.selectById(carrierId);
            if (carrier == null) throw new BizException("承运商不存在");
            if (!"合作中".equals(carrier.getStatus())) {
                throw new BizException("承运商「" + carrier.getName() + "」状态为" + carrier.getStatus() + "，不能接单");
            }
            assertValid(carrier.getLicenseExpire(), "承运商 " + carrier.getName() + " 的《道路运输经营许可证》");
        }
        if (vehicle == null && driver == null && carrier == null) {
            throw new BizException("请选择自有车辆/司机或外协承运商");
        }

        if ("已受理".equals(wb.getStatus())) {
            waybillService.transition(wb.getId(), "submit", "提交调度", null);
        }

        Dispatch dispatch = new Dispatch();
        dispatch.setDispatchNo(numberService.next("PC", TenantContext.requireTenantId()));
        dispatch.setWaybillId(waybillId);
        dispatch.setVehicleId(vehicleId);
        dispatch.setDriverId(driverId);
        dispatch.setCarrierId(carrierId);
        dispatch.setStatus("待接单");
        dispatch.setSource(carrierId != null ? "外协承运商" : "自有车辆");
        dispatch.setAssignee(TenantContext.username());
        dispatch.setAssignedAt(LocalDateTime.now());
        dispatch.setRemark(remark);
        dispatch.setEmptyFee(BigDecimal.ZERO);
        dispatchMapper.insert(dispatch);

        wb.setDriverId(driverId);
        wb.setDriverName(driver == null ? null : driver.getName());
        wb.setDriverPhone(driver == null ? null : driver.getPhone());
        wb.setPlateNo(vehicle == null ? null : vehicle.getPlateNo());
        wb.setCarrierId(carrierId);
        wb.setCarrierName(carrier == null ? null : carrier.getName());
        waybillMapper.updateById(wb);
        waybillService.transition(wb.getId(), "dispatch", "调度派车：" + dispatch.getDispatchNo(), null);

        if (vehicle != null) {
            vehicle.setCurrentStatus("在途");
            vehicleMapper.updateById(vehicle);
        }
        messageService.notify("派车通知", wb.getId(), wb.getSenderPhone(), wb.getWaybillNo(),
                (driver == null ? "" : driver.getName()) + " " + (vehicle == null ? "" : vehicle.getPlateNo()));
        auditService.log("调度派车", wb.getWaybillNo(),
                "派车单：" + dispatch.getDispatchNo() + "，司机：" + (driver == null ? "-" : driver.getName())
                        + "，车辆：" + (vehicle == null ? "-" : vehicle.getPlateNo()));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("dispatchId", dispatch.getId());
        result.put("dispatchNo", dispatch.getDispatchNo());
        result.put("waybillNo", wb.getWaybillNo());
        result.put("status", wb.getStatus());
        return result;
    }

    @Transactional
    public void reassign(Long dispatchId, Long vehicleId, Long driverId, Long carrierId, String reason) {
        Dispatch old = dispatchMapper.selectById(dispatchId);
        if (old == null) throw BizException.notFound("派车单不存在");
        if (List.of("已完成", "已取消").contains(old.getStatus())) {
            throw new BizException("派车单已" + old.getStatus() + "，不能改派");
        }
        old.setStatus("已改派");
        old.setRemark((old.getRemark() == null ? "" : old.getRemark()) + "；改派原因：" + reason);
        dispatchMapper.updateById(old);
        if (old.getVehicleId() != null) {
            Vehicle vehicle = vehicleMapper.selectById(old.getVehicleId());
            if (vehicle != null) {
                vehicle.setCurrentStatus("空闲");
                vehicleMapper.updateById(vehicle);
            }
        }
        Waybill wb = waybillMapper.selectById(old.getWaybillId());
        assign(wb.getId(), vehicleId, driverId, carrierId, "改派：" + reason);
        auditService.log("改派", wb.getWaybillNo(), reason);
    }

    @Transactional
    public void cancel(Long dispatchId, String reason, BigDecimal emptyFee) {
        Dispatch dispatch = dispatchMapper.selectById(dispatchId);
        if (dispatch == null) throw BizException.notFound("派车单不存在");
        dispatch.setStatus("已取消");
        dispatch.setEmptyFee(emptyFee == null ? BigDecimal.ZERO : emptyFee);
        dispatch.setRemark((dispatch.getRemark() == null ? "" : dispatch.getRemark()) + "；取消原因：" + reason);
        dispatchMapper.updateById(dispatch);
        Waybill wb = waybillMapper.selectById(dispatch.getWaybillId());
        if (wb != null) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("emptyFee", dispatch.getEmptyFee());
            waybillService.transition(wb.getId(), "cancel", reason, payload);
        }
        auditService.log("取消派车", dispatch.getDispatchNo(), "空驶费：" + dispatch.getEmptyFee() + "，原因：" + reason);
    }

    public Map<String, Object> board() {
        List<Vehicle> vehicles = vehicleMapper.selectList(null);
        List<Waybill> active = waybillMapper.selectList(new LambdaQueryWrapper<Waybill>()
                .in(Waybill::getStatus, "已受理", "待调度", "已派车", "已提货", "运输中", "中转中", "到达待派送", "派送中", "异常处理中"));
        Map<String, List<Map<String, Object>>> columns = new LinkedHashMap<>();
        columns.put("待调度", new ArrayList<>());
        columns.put("已派车", new ArrayList<>());
        columns.put("在途", new ArrayList<>());
        columns.put("异常", new ArrayList<>());
        for (Waybill wb : active) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", wb.getId());
            m.put("waybillNo", wb.getWaybillNo());
            m.put("customerName", wb.getCustomerName());
            m.put("origin", wb.getOrigin());
            m.put("destination", wb.getDestination());
            m.put("status", wb.getStatus());
            m.put("plateNo", wb.getPlateNo());
            m.put("driverName", wb.getDriverName());
            m.put("weight", wb.getWeight());
            m.put("pickupTime", wb.getPickupTime());
            String key = switch (wb.getStatus()) {
                case "已受理", "待调度" -> "待调度";
                case "已派车" -> "已派车";
                case "异常处理中" -> "异常";
                default -> "在途";
            };
            columns.get(key).add(m);
        }
        List<Map<String, Object>> vehicleViews = new ArrayList<>();
        for (Vehicle v : vehicles) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", v.getId());
            m.put("plateNo", v.getPlateNo());
            m.put("vehicleType", v.getVehicleType());
            m.put("status", v.getCurrentStatus() == null ? "空闲" : v.getCurrentStatus());
            m.put("lat", v.getCurrentLat());
            m.put("lng", v.getCurrentLng());
            String current = active.stream().filter(w -> v.getPlateNo() != null && v.getPlateNo().equals(w.getPlateNo()))
                    .map(Waybill::getWaybillNo).findFirst().orElse(null);
            m.put("waybillNo", current);
            vehicleViews.add(m);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("vehicles", vehicleViews);
        result.put("columns", columns);
        result.put("counts", columns.entrySet().stream().collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue().size()), Map::putAll));
        return result;
    }

    public List<Waybill> pool() {
        return waybillMapper.selectList(new LambdaQueryWrapper<Waybill>()
                .in(Waybill::getStatus, "已受理", "待调度").orderByAsc(Waybill::getPickupTime));
    }

    private void assertValid(LocalDateTime expire, String subject) {
        if (expire == null) return;
        if (expire.isBefore(LocalDateTime.now())) {
            throw new BizException(subject + "已于 " + expire.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 到期，不能派车");
        }
    }
}
