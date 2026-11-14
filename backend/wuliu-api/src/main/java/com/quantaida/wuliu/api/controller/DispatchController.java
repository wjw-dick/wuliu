package com.quantaida.wuliu.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quantaida.wuliu.common.result.PageResult;
import com.quantaida.wuliu.common.web.BaseCrudController;
import com.quantaida.wuliu.domain.entity.*;
import com.quantaida.wuliu.domain.mapper.*;
import com.quantaida.wuliu.service.dispatch.DispatchService;
import com.quantaida.wuliu.service.report.DashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/** 调度与运力：待调度池、看板、派车、改派、车辆/司机/承运商、资质到期。 */
@RestController
@RequestMapping("/api/tenant")
public class DispatchController extends BaseCrudController<Vehicle> {
    private final VehicleMapper vehicleMapper;
    private final DriverMapper driverMapper;
    private final CarrierMapper carrierMapper;
    private final DispatchService dispatchService;
    private final DashboardService dashboardService;

    public DispatchController(VehicleMapper vehicleMapper, DriverMapper driverMapper,
                              CarrierMapper carrierMapper, DispatchService dispatchService,
                              DashboardService dashboardService) {
        super(vehicleMapper, Vehicle.class);
        this.vehicleMapper = vehicleMapper;
        this.driverMapper = driverMapper;
        this.carrierMapper = carrierMapper;
        this.dispatchService = dispatchService;
        this.dashboardService = dashboardService;
    }

    @Override
    protected String[] keywordFields() {
        return new String[]{"plateNo", "vehicleType", "fleet"};
    }

    @GetMapping("/vehicles")
    @Override
    public PageResult<Vehicle> list(@RequestParam Map<String, Object> params) {
        return super.list(params);
    }

    @PostMapping("/vehicles")
    @Override
    public Vehicle create(@RequestBody Vehicle vehicle) {
        vehicle.setId(null);
        if (vehicle.getStatus() == null) vehicle.setStatus("正常");
        return super.create(vehicle);
    }

    @PutMapping("/vehicles/{id}")
    public Vehicle updateVehicle(@PathVariable Long id, @RequestBody Vehicle vehicle) {
        return update(id, vehicle);
    }

    @DeleteMapping("/vehicles/{id}")
    public Map<String, Object> deleteVehicle(@PathVariable Long id) {
        return delete(id);
    }

    @GetMapping("/drivers")
    public PageResult<Driver> drivers(@RequestParam Map<String, Object> params) {
        var qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Driver>();
        String keyword = params.get("keyword") == null ? "" : String.valueOf(params.get("keyword"));
        if (!keyword.isBlank()) qw.and(w -> w.like("name", keyword).or().like("phone", keyword));
        qw.orderByDesc("id");
        var page = driverMapper.selectPage(new Page<>(pl(params.get("page"), 1),
                pl(params.get("size"), 10)), qw);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @PostMapping("/drivers")
    public Driver createDriver(@RequestBody Driver driver) {
        driver.setId(null);
        if (driver.getStatus() == null) driver.setStatus("在职");
        driverMapper.insert(driver);
        return driver;
    }

    @PutMapping("/drivers/{id}")
    public Driver updateDriver(@PathVariable Long id, @RequestBody Driver driver) {
        driver.setId(id);
        driverMapper.updateById(driver);
        return driverMapper.selectById(id);
    }

    @DeleteMapping("/drivers/{id}")
    public Map<String, Object> deleteDriver(@PathVariable Long id) {
        driverMapper.deleteById(id);
        return Map.of("deleted", true);
    }

    @GetMapping("/carriers")
    public PageResult<Carrier> carriers(@RequestParam Map<String, Object> params) {
        var qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Carrier>();
        String keyword = params.get("keyword") == null ? "" : String.valueOf(params.get("keyword"));
        if (!keyword.isBlank()) qw.and(w -> w.like("name", keyword).or().like("contact_name", keyword));
        String status = params.get("status") == null ? "" : String.valueOf(params.get("status"));
        if (!status.isBlank()) qw.eq("status", status);
        qw.orderByDesc("id");
        var page = carrierMapper.selectPage(new Page<>(pl(params.get("page"), 1),
                pl(params.get("size"), 10)), qw);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @PostMapping("/carriers")
    public Carrier createCarrier(@RequestBody Carrier carrier) {
        carrier.setId(null);
        if (carrier.getStatus() == null) carrier.setStatus("待审核");
        carrierMapper.insert(carrier);
        return carrier;
    }

    @PutMapping("/carriers/{id}")
    public Carrier updateCarrier(@PathVariable Long id, @RequestBody Carrier carrier) {
        carrier.setId(id);
        carrierMapper.updateById(carrier);
        return carrierMapper.selectById(id);
    }

    @DeleteMapping("/carriers/{id}")
    public Map<String, Object> deleteCarrier(@PathVariable Long id) {
        carrierMapper.deleteById(id);
        return Map.of("deleted", true);
    }

    @GetMapping("/qualifications/expiring")
    public List<Map<String, Object>> expiring() {
        return dashboardService.expiringQualifications();
    }

    @GetMapping("/dispatch/pool")
    public List<Waybill> pool() {
        return dispatchService.pool();
    }

    @GetMapping("/dispatch/board")
    public Map<String, Object> board() {
        return dispatchService.board();
    }

    @PostMapping("/dispatch/assign")
    public Map<String, Object> assign(@RequestBody Map<String, Object> body) {
        Long waybillId = Long.valueOf(String.valueOf(body.get("waybillId")));
        Long vehicleId = body.get("vehicleId") == null ? null : Long.valueOf(String.valueOf(body.get("vehicleId")));
        Long driverId = body.get("driverId") == null ? null : Long.valueOf(String.valueOf(body.get("driverId")));
        Long carrierId = body.get("carrierId") == null ? null : Long.valueOf(String.valueOf(body.get("carrierId")));
        return dispatchService.assign(waybillId, vehicleId, driverId, carrierId,
                body.get("remark") == null ? null : String.valueOf(body.get("remark")));
    }

    @PostMapping("/dispatch/{id}/reassign")
    public Map<String, Object> reassign(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long vehicleId = body.get("vehicleId") == null ? null : Long.valueOf(String.valueOf(body.get("vehicleId")));
        Long driverId = body.get("driverId") == null ? null : Long.valueOf(String.valueOf(body.get("driverId")));
        Long carrierId = body.get("carrierId") == null ? null : Long.valueOf(String.valueOf(body.get("carrierId")));
        dispatchService.reassign(id, vehicleId, driverId, carrierId,
                body.get("reason") == null ? "" : String.valueOf(body.get("reason")));
        return Map.of("reassigned", true);
    }

    @PostMapping("/dispatch/{id}/cancel")
    public Map<String, Object> cancelDispatch(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        dispatchService.cancel(id, String.valueOf(body.getOrDefault("reason", "")),
                body.get("emptyFee") == null ? null : new java.math.BigDecimal(String.valueOf(body.get("emptyFee"))));
        return Map.of("cancelled", true);
    }

    private static long pl(Object v, long def) {
        try {
            return v == null ? def : Long.parseLong(String.valueOf(v));
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
