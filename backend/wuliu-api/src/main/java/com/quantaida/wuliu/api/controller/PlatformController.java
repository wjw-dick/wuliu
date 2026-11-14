package com.quantaida.wuliu.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.common.result.PageResult;
import com.quantaida.wuliu.domain.entity.*;
import com.quantaida.wuliu.domain.mapper.*;
import com.quantaida.wuliu.service.auth.AuthService;
import com.quantaida.wuliu.service.system.AuditService;
import com.quantaida.wuliu.service.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/** 平台运营端：只做租户/套餐/账单/审计等运营事务，不提供租户业务明细下钻。 */
@RestController
@RequestMapping("/api/platform")
@RequiredArgsConstructor
public class PlatformController {
    private final TenantMapper tenantMapper;
    private final TenantLicenseMapper licenseMapper;
    private final PlanMapper planMapper;
    private final BillMapper billMapper;
    private final SeatOrderMapper seatOrderMapper;
    private final PlatformUserMapper platformUserMapper;
    private final PlatformRoleMapper platformRoleMapper;
    private final AuditLogMapper auditLogMapper;
    private final AnnouncementMapper announcementMapper;
    private final PlatformConfigMapper platformConfigMapper;
    private final PlatformDictMapper platformDictMapper;
    private final TenantRoleMapper tenantRoleMapper;
    private final TenantUserMapper tenantUserMapper;
    private final WaybillMapper waybillMapper;
    private final ExceptionOrderMapper exceptionMapper;
    private final TenantService tenantService;
    private final AuditService auditService;

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        List<Tenant> tenants = tenantMapper.selectList(null);
        Map<String, Long> status = new LinkedHashMap<>();
        for (Tenant t : tenants) status.merge(t.getStatus() == null ? "未知" : t.getStatus(), 1L, Long::sum);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("tenantTotal", tenants.size());
        m.put("tenantByStatus", status);
        m.put("trialCount", tenants.stream().filter(t -> "试用中".equals(t.getStatus())).count());
        m.put("overdueCount", tenants.stream().filter(t -> "欠费".equals(t.getStatus())).count());
        m.put("expiringCount", tenants.stream().filter(t -> {
            if (t.getServiceEnd() == null) return false;
            long days = java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), t.getServiceEnd());
            return days >= 0 && days <= 30;
        }).count());
        m.put("waybillTotal", waybillMapper.selectCount(null));
        m.put("exceptionTotal", exceptionMapper.selectCount(new LambdaQueryWrapper<ExceptionOrder>()
                .eq(ExceptionOrder::getStatus, "待处理")));
        m.put("expiringTenants", tenants.stream().filter(t -> t.getServiceEnd() != null
                        && t.getServiceEnd().isBefore(LocalDateTime.now().plusDays(30)))
                .map(t -> Map.of("id", t.getId(), "name", t.getName(), "status", t.getStatus(),
                        "serviceEnd", t.getServiceEnd())).limit(8).toList());
        return m;
    }

    @GetMapping("/tenants")
    public PageResult<Tenant> tenants(@RequestParam Map<String, Object> params) {
        long page = parseLong(params.get("page"), 1);
        long size = parseLong(params.get("size"), 10);
        LambdaQueryWrapper<Tenant> qw = new LambdaQueryWrapper<>();
        String keyword = params.get("keyword") == null ? "" : String.valueOf(params.get("keyword"));
        if (!keyword.isBlank()) qw.and(w -> w.like(Tenant::getName, keyword).or().like(Tenant::getCode, keyword));
        String status = params.get("status") == null ? "" : String.valueOf(params.get("status"));
        if (!status.isBlank()) qw.eq(Tenant::getStatus, status);
        qw.orderByDesc(Tenant::getId);
        Page<Tenant> pageRes = tenantMapper.selectPage(new Page<>(page, size), qw);
        return PageResult.of(pageRes.getRecords(), pageRes.getTotal(), pageRes.getCurrent(), pageRes.getSize());
    }

    @Transactional
    @PostMapping("/tenants")
    public Tenant createTenant(@RequestBody Map<String, Object> body) {
        String name = str(body.get("name"));
        if (name.isBlank()) throw new BizException("请填写企业名称");
        Tenant tenant = new Tenant();
        tenant.setCode(str(body.getOrDefault("code", "T" + System.currentTimeMillis() % 100000)));
        tenant.setName(name);
        tenant.setCreditCode(str(body.get("creditCode")));
        tenant.setIndustry(str(body.getOrDefault("industry", "综合物流")));
        tenant.setStatus("试用中");
        tenant.setPlanId(body.get("planId") == null ? 2L : Long.valueOf(String.valueOf(body.get("planId"))));
        tenant.setSeatLimit(body.get("seatLimit") == null ? 10 : Integer.parseInt(String.valueOf(body.get("seatLimit"))));
        tenant.setSeatUsed(1);
        tenant.setContactName(str(body.get("contactName")));
        tenant.setContactPhone(str(body.get("contactPhone")));
        tenant.setTrialEnd(LocalDateTime.now().plusDays(15));
        tenant.setServiceStart(LocalDateTime.now());
        tenant.setServiceEnd(LocalDateTime.now().plusMonths(1));
        tenant.setRemark("平台开通：试用期 15 天");
        tenantMapper.insert(tenant);

        TenantLicense license = new TenantLicense();
        license.setTenantId(tenant.getId());
        license.setLicenseNo(str(body.get("licenseNo")));
        license.setBusinessLicenseUrl(str(body.get("businessLicenseUrl")));
        license.setLegalIdUrl(str(body.get("legalIdUrl")));
        license.setStatus("待审核");
        licenseMapper.insert(license);

        TenantRole role = new TenantRole();
        role.setName("企业管理员");
        role.setMenus("[\"*\"]");
        role.setButtons("[\"*\"]");
        role.setDataScope("公司级");
        role.setStatus("启用");
        role.setTenantId(tenant.getId());
        tenantRoleMapper.insert(role);

        TenantUser admin = new TenantUser();
        admin.setTenantId(tenant.getId());
        admin.setUsername(str(body.getOrDefault("adminUsername", "admin")));
        admin.setPassword(AuthService.hash("admin123"));
        admin.setNickname("企业管理员");
        admin.setPhone(str(body.get("contactPhone")));
        admin.setRoleId(role.getId());
        admin.setStatus("正常");
        tenantUserMapper.insert(admin);
        auditService.log("开通租户", tenant.getCode(), name + "，套餐：" + tenant.getPlanId());
        return tenant;
    }

    @PutMapping("/tenants/{id}")
    public Tenant updateTenant(@PathVariable Long id, @RequestBody Tenant patch) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant == null) throw BizException.notFound("租户不存在");
        patch.setId(id);
        patch.setCode(tenant.getCode());
        tenantMapper.updateById(patch);
        auditService.log("编辑租户", tenant.getCode(), "");
        return tenantMapper.selectById(id);
    }

    @DeleteMapping("/tenants/{id}")
    public Map<String, Object> deleteTenant(@PathVariable Long id) {
        Tenant tenant = tenantMapper.selectById(id);
        if (tenant == null) throw BizException.notFound("租户不存在");
        tenant.setStatus("已停用");
        tenant.setRemark((tenant.getRemark() == null ? "" : tenant.getRemark()) + "；已删除归档（业务数据按合同保留，审计与账单永久保留）");
        tenantMapper.updateById(tenant);
        auditService.log("删除租户", tenant.getCode(), "数据归档");
        return Map.of("archived", true);
    }

    @PostMapping("/tenants/{id}/renew")
    public Tenant renew(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Tenant tenant = tenantService.renew(id, (int) parseLong(body.get("months"), 1));
        auditService.log("租户续期", tenant.getCode(), body.get("months") + " 个月");
        return tenant;
    }

    @PostMapping("/tenants/{id}/suspend")
    public Tenant suspend(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        tenantService.suspend(id, str(body.get("reason")));
        return tenantMapper.selectById(id);
    }

    @PostMapping("/tenants/{id}/enable")
    public Tenant enable(@PathVariable Long id) {
        tenantService.enable(id);
        return tenantMapper.selectById(id);
    }

    @GetMapping("/tenants/{id}/license")
    public TenantLicense license(@PathVariable Long id) {
        return licenseMapper.selectOne(new LambdaQueryWrapper<TenantLicense>()
                .eq(TenantLicense::getTenantId, id).orderByDesc(TenantLicense::getId).last("limit 1"));
    }

    @GetMapping("/licenses")
    public PageResult<TenantLicense> licenses(@RequestParam(defaultValue = "待审核") String status,
                                              @RequestParam Map<String, Object> params) {
        LambdaQueryWrapper<TenantLicense> qw = new LambdaQueryWrapper<>();
        if (status != null && !status.isBlank()) qw.eq(TenantLicense::getStatus, status);
        qw.orderByAsc(TenantLicense::getId);
        Page<TenantLicense> page = licenseMapper.selectPage(new Page<>(parseLong(params.get("page"), 1),
                parseLong(params.get("size"), 10)), qw);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @PostMapping("/licenses/{id}/review")
    public TenantLicense review(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        TenantLicense license = licenseMapper.selectById(id);
        if (license == null) throw BizException.notFound("资质记录不存在");
        boolean pass = Boolean.parseBoolean(String.valueOf(body.getOrDefault("pass", "false")));
        license.setStatus(pass ? "已通过" : "已驳回");
        license.setRejectReason(pass ? "" : str(body.get("reason")));
        license.setReviewedBy("平台运营");
        license.setReviewedAt(LocalDateTime.now());
        licenseMapper.updateById(license);
        auditService.log("资质审核", license.getLicenseNo(), pass ? "通过" : "驳回：" + license.getRejectReason());
        return license;
    }

    @GetMapping("/plans")
    public List<Plan> plans() {
        return planMapper.selectList(new LambdaQueryWrapper<Plan>().orderByAsc(Plan::getSortNo));
    }

    @PostMapping("/plans")
    public Plan createPlan(@RequestBody Plan plan) {
        plan.setId(null);
        planMapper.insert(plan);
        return plan;
    }

    @PutMapping("/plans/{id}")
    public Plan updatePlan(@PathVariable Long id, @RequestBody Plan plan) {
        plan.setId(id);
        planMapper.updateById(plan);
        return planMapper.selectById(id);
    }

    @DeleteMapping("/plans/{id}")
    public Map<String, Object> deletePlan(@PathVariable Long id) {
        planMapper.deleteById(id);
        return Map.of("deleted", true);
    }

    @GetMapping("/seats")
    public List<SeatOrder> seats() {
        return seatOrderMapper.selectList(new LambdaQueryWrapper<SeatOrder>().orderByDesc(SeatOrder::getId));
    }

    @PostMapping("/seats")
    public SeatOrder addSeats(@RequestBody SeatOrder order) {
        order.setId(null);
        if (order.getTenantId() == null) throw new BizException("请选择租户");
        Tenant tenant = tenantMapper.selectById(order.getTenantId());
        if (tenant != null) {
            tenant.setSeatLimit((tenant.getSeatLimit() == null ? 0 : tenant.getSeatLimit())
                    + (order.getQuantity() == null ? 0 : order.getQuantity()));
            tenantMapper.updateById(tenant);
        }
        seatOrderMapper.insert(order);
        auditService.log("席位增购", String.valueOf(order.getTenantId()), "数量：" + order.getQuantity());
        return order;
    }

    @GetMapping("/bills")
    public PageResult<Bill> bills(@RequestParam Map<String, Object> params) {
        LambdaQueryWrapper<Bill> qw = new LambdaQueryWrapper<>();
        String status = params.get("status") == null ? "" : String.valueOf(params.get("status"));
        if (!status.isBlank()) qw.eq(Bill::getStatus, status);
        qw.orderByDesc(Bill::getId);
        Page<Bill> page = billMapper.selectPage(new Page<>(parseLong(params.get("page"), 1),
                parseLong(params.get("size"), 10)), qw);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @PostMapping("/bills")
    public Bill createBill(@RequestBody Bill bill) {
        bill.setId(null);
        if (bill.getStatus() == null) bill.setStatus("待支付");
        billMapper.insert(bill);
        return bill;
    }

    @PostMapping("/bills/{id}/pay")
    public Bill pay(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Bill bill = billMapper.selectById(id);
        if (bill == null) throw BizException.notFound("账单不存在");
        bill.setPaidAmount(new BigDecimal(String.valueOf(body.getOrDefault("amount", bill.getAmount()))));
        bill.setStatus("已结清");
        bill.setPaidAt(LocalDateTime.now());
        billMapper.updateById(bill);
        Tenant tenant = tenantMapper.selectById(bill.getTenantId());
        if (tenant != null && "欠费".equals(tenant.getStatus())) {
            tenant.setStatus("正常");
            tenantMapper.updateById(tenant);
        }
        auditService.log("账单收款", bill.getBillNo(), bill.getPaidAmount() + " 元");
        return bill;
    }

    @GetMapping("/users")
    public List<PlatformUser> users() {
        return platformUserMapper.selectList(new LambdaQueryWrapper<PlatformUser>());
    }

    @PostMapping("/users")
    public PlatformUser createUser(@RequestBody PlatformUser user) {
        user.setId(null);
        user.setPassword(AuthService.hash(user.getPassword() == null ? "admin123" : user.getPassword()));
        if (user.getStatus() == null) user.setStatus("正常");
        platformUserMapper.insert(user);
        return user;
    }

    @PutMapping("/users/{id}")
    public PlatformUser updateUser(@PathVariable Long id, @RequestBody PlatformUser user) {
        PlatformUser old = platformUserMapper.selectById(id);
        if (old == null) throw BizException.notFound("账号不存在");
        user.setId(id);
        if (user.getPassword() == null || user.getPassword().isBlank()) user.setPassword(old.getPassword());
        else user.setPassword(AuthService.hash(user.getPassword()));
        platformUserMapper.updateById(user);
        return platformUserMapper.selectById(id);
    }

    @DeleteMapping("/users/{id}")
    public Map<String, Object> deleteUser(@PathVariable Long id) {
        platformUserMapper.deleteById(id);
        return Map.of("deleted", true);
    }

    @GetMapping("/roles")
    public List<PlatformRole> roles() {
        return platformRoleMapper.selectList(new LambdaQueryWrapper<PlatformRole>());
    }

    @PostMapping("/roles")
    public PlatformRole createRole(@RequestBody PlatformRole role) {
        role.setId(null);
        platformRoleMapper.insert(role);
        return role;
    }

    @DeleteMapping("/roles/{id}")
    public Map<String, Object> deleteRole(@PathVariable Long id) {
        platformRoleMapper.deleteById(id);
        return Map.of("deleted", true);
    }

    @GetMapping("/audits")
    public PageResult<AuditLog> audits(@RequestParam Map<String, Object> params) {
        LambdaQueryWrapper<AuditLog> qw = new LambdaQueryWrapper<>();
        if (params.get("tenantId") != null) qw.eq(AuditLog::getTenantId, params.get("tenantId"));
        if (params.get("action") != null && !String.valueOf(params.get("action")).isBlank()) {
            qw.like(AuditLog::getAction, params.get("action"));
        }
        if (params.get("operatorName") != null && !String.valueOf(params.get("operatorName")).isBlank()) {
            qw.like(AuditLog::getOperatorName, params.get("operatorName"));
        }
        qw.orderByDesc(AuditLog::getId);
        Page<AuditLog> page = auditLogMapper.selectPage(new Page<>(parseLong(params.get("page"), 1),
                parseLong(params.get("size"), 10)), qw);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @GetMapping("/announcements")
    public List<Announcement> announcements() {
        return announcementMapper.selectList(new LambdaQueryWrapper<Announcement>().orderByDesc(Announcement::getId));
    }

    @PostMapping("/announcements")
    public Announcement createAnnouncement(@RequestBody Announcement announcement) {
        announcement.setId(null);
        announcement.setTenantId(0L);
        if (announcement.getStatus() == null) announcement.setStatus("已发布");
        if (announcement.getPublishedAt() == null) announcement.setPublishedAt(LocalDateTime.now());
        announcementMapper.insert(announcement);
        return announcement;
    }

    @DeleteMapping("/announcements/{id}")
    public Map<String, Object> deleteAnnouncement(@PathVariable Long id) {
        announcementMapper.deleteById(id);
        return Map.of("deleted", true);
    }

    @GetMapping("/dicts")
    public List<PlatformDict> dicts() {
        return platformDictMapper.selectList(new LambdaQueryWrapper<PlatformDict>());
    }

    @PostMapping("/dicts")
    public PlatformDict createDict(@RequestBody PlatformDict dict) {
        dict.setId(null);
        platformDictMapper.insert(dict);
        return dict;
    }

    @GetMapping("/configs")
    public List<PlatformConfig> configs() {
        return platformConfigMapper.selectList(new LambdaQueryWrapper<PlatformConfig>());
    }

    @PutMapping("/configs")
    public List<PlatformConfig> saveConfigs(@RequestBody List<PlatformConfig> configs) {
        for (PlatformConfig c : configs) {
            PlatformConfig old = platformConfigMapper.selectOne(new LambdaQueryWrapper<PlatformConfig>()
                    .eq(PlatformConfig::getConfigKey, c.getConfigKey()).last("limit 1"));
            if (old == null) {
                c.setId(null);
                platformConfigMapper.insert(c);
            } else {
                c.setId(old.getId());
                platformConfigMapper.updateById(c);
            }
        }
        auditService.log("平台参数保存", "", "");
        return configs;
    }

    private static long parseLong(Object v, long def) {
        try {
            return v == null ? def : Long.parseLong(String.valueOf(v));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static String str(Object v) {
        return v == null ? "" : String.valueOf(v);
    }
}
