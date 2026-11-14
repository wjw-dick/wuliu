package com.quantaida.wuliu.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.common.result.PageResult;
import com.quantaida.wuliu.domain.entity.*;
import com.quantaida.wuliu.domain.mapper.*;
import com.quantaida.wuliu.service.auth.AuthService;
import com.quantaida.wuliu.service.report.DashboardService;
import com.quantaida.wuliu.service.system.AuditService;
import com.quantaida.wuliu.service.system.MenuService;
import com.quantaida.wuliu.service.tenant.TenantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/** 租户后台：工作台、组织、账号与角色、公告。 */
@RestController
@RequestMapping("/api/tenant")
@RequiredArgsConstructor
public class TenantCommonController {
    private final DashboardService dashboardService;
    private final TenantService tenantService;
    private final OrgMapper orgMapper;
    private final TenantUserMapper userMapper;
    private final TenantRoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final AnnouncementMapper announcementMapper;
    private final MenuService menuService;
    private final AuditService auditService;

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return dashboardService.tenantDashboard();
    }

    @GetMapping("/orgs")
    public List<Org> orgs() {
        return orgMapper.selectList(new LambdaQueryWrapper<Org>().orderByAsc(Org::getId));
    }

    @PostMapping("/orgs")
    public Org createOrg(@RequestBody Org org) {
        org.setId(null);
        if (org.getStatus() == null) org.setStatus("启用");
        orgMapper.insert(org);
        return org;
    }

    @PutMapping("/orgs/{id}")
    public Org updateOrg(@PathVariable Long id, @RequestBody Org org) {
        org.setId(id);
        orgMapper.updateById(org);
        return orgMapper.selectById(id);
    }

    @DeleteMapping("/orgs/{id}")
    public Map<String, Object> deleteOrg(@PathVariable Long id) {
        orgMapper.deleteById(id);
        return Map.of("deleted", true);
    }

    @GetMapping("/users")
    public PageResult<TenantUser> users(@RequestParam Map<String, Object> params) {
        LambdaQueryWrapper<TenantUser> qw = new LambdaQueryWrapper<>();
        String keyword = params.get("keyword") == null ? "" : String.valueOf(params.get("keyword"));
        if (!keyword.isBlank()) qw.and(w -> w.like(TenantUser::getNickname, keyword).or().like(TenantUser::getUsername, keyword));
        qw.orderByDesc(TenantUser::getId);
        Page<TenantUser> page = userMapper.selectPage(new Page<>(parseLong(params.get("page"), 1),
                parseLong(params.get("size"), 10)), qw);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    @PostMapping("/users")
    public TenantUser createUser(@RequestBody TenantUser user) {
        tenantService.assertSeatAvailable();
        Long exists = userMapper.selectCount(new LambdaQueryWrapper<TenantUser>()
                .eq(TenantUser::getUsername, user.getUsername()));
        if (exists != null && exists > 0) throw new BizException("账号名已存在");
        user.setId(null);
        user.setPassword(AuthService.hash(user.getPassword() == null ? "admin123" : user.getPassword()));
        if (user.getStatus() == null) user.setStatus("正常");
        userMapper.insert(user);
        auditService.log("新增账号", user.getUsername(), user.getNickname());
        return user;
    }

    @PutMapping("/users/{id}")
    public TenantUser updateUser(@PathVariable Long id, @RequestBody TenantUser user) {
        TenantUser old = userMapper.selectById(id);
        if (old == null) throw BizException.notFound("账号不存在");
        user.setId(id);
        if (user.getPassword() == null || user.getPassword().isBlank()) user.setPassword(old.getPassword());
        else user.setPassword(AuthService.hash(user.getPassword()));
        userMapper.updateById(user);
        return userMapper.selectById(id);
    }

    @DeleteMapping("/users/{id}")
    public Map<String, Object> deleteUser(@PathVariable Long id) {
        TenantUser user = userMapper.selectById(id);
        if (user != null) {
            user.setStatus("停用");
            userMapper.updateById(user);
        }
        return Map.of("deleted", true);
    }

    @GetMapping("/roles")
    public List<TenantRole> roles() {
        return roleMapper.selectList(new LambdaQueryWrapper<TenantRole>());
    }

    @PostMapping("/roles")
    public TenantRole createRole(@RequestBody TenantRole role) {
        role.setId(null);
        roleMapper.insert(role);
        return role;
    }

    @PutMapping("/roles/{id}")
    public TenantRole updateRole(@PathVariable Long id, @RequestBody TenantRole role) {
        role.setId(id);
        roleMapper.updateById(role);
        return roleMapper.selectById(id);
    }

    @DeleteMapping("/roles/{id}")
    public Map<String, Object> deleteRole(@PathVariable Long id) {
        roleMapper.deleteById(id);
        return Map.of("deleted", true);
    }

    @GetMapping("/menus/tree")
    public List<Menu> menus() {
        return menuMapper.selectList(new LambdaQueryWrapper<Menu>().orderByAsc(Menu::getSortNo));
    }

    @GetMapping("/announcements")
    public List<Announcement> announcements() {
        return announcementMapper.selectList(new LambdaQueryWrapper<Announcement>()
                .and(w -> w.eq(Announcement::getTenantId, 0L)
                        .or().eq(Announcement::getTenantId, com.quantaida.wuliu.common.context.TenantContext.requireTenantId()))
                .eq(Announcement::getStatus, "已发布")
                .orderByDesc(Announcement::getId));
    }

    @GetMapping("/menu/channels")
    public List<String> channels() {
        return List.of("官网导航", "小程序 tabBar", "H5 底部导航", "公众号菜单");
    }

    @GetMapping("/system/menus")
    public Map<String, Object> menus(@RequestParam(defaultValue = "官网导航") String channel) {
        return menuService.tree(channel);
    }

    @PutMapping("/system/menus")
    public Map<String, Object> saveMenus(@RequestBody Map<String, Object> body) {
        String channel = String.valueOf(body.getOrDefault("channel", "官网导航"));
        List<Map<String, Object>> items = (List<Map<String, Object>>) body.get("items");
        menuService.saveChannel(channel, items);
        return Map.of("saved", true, "channel", channel);
    }

    private static long parseLong(Object v, long def) {
        try {
            return v == null ? def : Long.parseLong(String.valueOf(v));
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
