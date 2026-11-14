package com.quantaida.wuliu.api.controller;

import com.quantaida.wuliu.common.context.TenantContext;
import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.domain.entity.PlatformUser;
import com.quantaida.wuliu.domain.entity.Tenant;
import com.quantaida.wuliu.domain.entity.TenantRole;
import com.quantaida.wuliu.domain.entity.TenantUser;
import com.quantaida.wuliu.domain.mapper.PlatformUserMapper;
import com.quantaida.wuliu.domain.mapper.TenantMapper;
import com.quantaida.wuliu.domain.mapper.TenantRoleMapper;
import com.quantaida.wuliu.domain.mapper.TenantUserMapper;
import com.quantaida.wuliu.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/** 认证：登录、当前用户、改密、SSO 入口。 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final TenantUserMapper tenantUserMapper;
    private final TenantRoleMapper tenantRoleMapper;
    private final TenantMapper tenantMapper;
    private final PlatformUserMapper platformUserMapper;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        return authService.login(body.get("username"), body.get("password"),
                body.get("scope"), body.get("tenantCode"));
    }

    @PostMapping("/logout")
    public Map<String, Object> logout() {
        return Map.of("loggedOut", true);
    }

    @GetMapping("/me")
    public Map<String, Object> me() {
        String scope = TenantContext.scope();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("user", userInfo());
        m.put("roles", scope.equals("platform") ? List.of("平台运营") : roleNames());
        m.put("menus", scope.equals("platform") ? List.of("*") : menus());
        m.put("buttons", scope.equals("platform") ? List.of("*") : buttons());
        return m;
    }

    private Map<String, Object> userInfo() {
        Map<String, Object> user = new LinkedHashMap<>();
        String scope = TenantContext.scope();
        if ("platform".equals(scope)) {
            PlatformUser u = platformUserMapper.selectById(TenantContext.userId());
            user.put("id", u.getId());
            user.put("username", u.getUsername());
            user.put("nickname", u.getNickname());
            user.put("scope", "platform");
            user.put("tenantName", "平台运营中心");
        } else if ("tenant".equals(scope)) {
            TenantUser u = tenantUserMapper.selectById(TenantContext.userId());
            Tenant tenant = tenantMapper.selectById(u.getTenantId());
            user.put("id", u.getId());
            user.put("username", u.getUsername());
            user.put("nickname", u.getNickname());
            user.put("scope", "tenant");
            user.put("tenantId", u.getTenantId());
            user.put("tenantName", tenant == null ? "" : tenant.getName());
            user.put("tenantStatus", tenant == null ? "" : tenant.getStatus());
        } else {
            user.put("id", TenantContext.userId());
            user.put("username", TenantContext.username());
            user.put("scope", scope);
        }
        return user;
    }

    private TenantRole currentRole() {
        if (!"tenant".equals(TenantContext.scope())) return null;
        TenantUser u = tenantUserMapper.selectById(TenantContext.userId());
        return u == null || u.getRoleId() == null ? null : tenantRoleMapper.selectById(u.getRoleId());
    }

    private List<String> roleNames() {
        TenantRole role = currentRole();
        return role == null ? List.of("企业管理员") : List.of(role.getName());
    }

    private List<String> menus() {
        TenantRole role = currentRole();
        if (role == null || role.getMenus() == null) return List.of("*");
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(role.getMenus(), new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {
                    });
        } catch (Exception e) {
            return List.of("*");
        }
    }

    private List<String> buttons() {
        TenantRole role = currentRole();
        if (role == null || role.getButtons() == null) return List.of("*");
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(role.getButtons(), new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {
                    });
        } catch (Exception e) {
            return List.of();
        }
    }

    @PostMapping("/password")
    public Map<String, Object> password(@RequestBody Map<String, String> body) {
        authService.changePassword(body.get("oldPassword"), body.get("newPassword"));
        return Map.of("changed", true);
    }

    @PostMapping("/sso/{provider}")
    public Map<String, Object> sso(@PathVariable String provider) {
        throw new BizException(404, provider + " SSO 未配置或不可用，请使用账号密码登录");
    }
}
