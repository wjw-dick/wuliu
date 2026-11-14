package com.quantaida.wuliu.service.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.wuliu.common.context.TenantContext;
import com.quantaida.wuliu.common.crypto.AppCipher;
import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.domain.entity.*;
import com.quantaida.wuliu.domain.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/** 认证：平台/租户/司机/承运商四类登录入口，登录后签发 JWT。 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private final PlatformUserMapper platformUserMapper;
    private final TenantUserMapper tenantUserMapper;
    private final TenantMapper tenantMapper;
    private final TenantRoleMapper tenantRoleMapper;
    private final DriverMapper driverMapper;
    private final CarrierMapper carrierMapper;
    private final PhoneLookupService phoneLookupService;
    private final com.quantaida.wuliu.common.security.JwtUtil jwtUtil;

    public static String hash(String raw) {
        return ENCODER.encode(raw);
    }

    public Map<String, Object> login(String username, String password, String scope, String tenantCode) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new BizException("请输入账号与密码");
        }
        return switch (scope == null ? "tenant" : scope) {
            case "platform" -> platformLogin(username, password);
            case "tenant" -> tenantLogin(username, password, tenantCode);
            case "driver" -> driverLogin(username, password);
            case "carrier" -> carrierLogin(username, password);
            default -> throw new BizException("不支持的登录类型");
        };
    }

    private Map<String, Object> platformLogin(String username, String password) {
        PlatformUser user = platformUserMapper.selectOne(new LambdaQueryWrapper<PlatformUser>()
                .eq(PlatformUser::getUsername, username).last("limit 1"));
        if (user == null || !ENCODER.matches(password, user.getPassword())) {
            throw new BizException(401, "账号或密码错误");
        }
        if (!"正常".equals(user.getStatus())) {
            throw new BizException(403, "平台账号已停用，请联系管理员");
        }
        user.setLastLoginAt(LocalDateTime.now());
        platformUserMapper.updateById(user);
        String token = jwtUtil.issue(user.getId(), user.getUsername(), "platform", 0L, null, null);
        Map<String, Object> data = baseUser(user.getId(), user.getUsername(), user.getNickname(), "platform", 0L, "平台运营中心", null);
        data.put("token", token);
        data.put("roles", List.of(user.getRole() == null ? "平台运营" : user.getRole()));
        data.put("menus", List.of("*"));
        data.put("buttons", List.of("*"));
        data.put("tenantStatus", "正常");
        return data;
    }

    private Map<String, Object> tenantLogin(String username, String password, String tenantCode) {
        TenantUser user = tenantUserMapper.selectOne(new LambdaQueryWrapper<TenantUser>()
                .eq(TenantUser::getUsername, username).last("limit 1"));
        if (user == null || !ENCODER.matches(password, user.getPassword())) {
            throw new BizException(401, "账号或密码错误");
        }
        Tenant tenant = tenantMapper.selectById(user.getTenantId());
        if (tenant == null) throw new BizException(403, "账号所属企业不存在");
        if (tenantCode != null && !tenantCode.isBlank() && !tenantCode.equals(tenant.getCode())) {
            throw new BizException(403, "账号不属于该企业");
        }
        if (!"正常".equals(user.getStatus())) throw new BizException(403, "账号已停用，请联系企业管理员");
        if ("停用".equals(tenant.getStatus())) {
            throw new BizException(403, "账号所属企业已停用，请联系服务商");
        }
        if ("已到期".equals(tenant.getStatus())) {
            throw new BizException(403, "账号所属企业服务已到期，7 天宽限期内请联系服务商续费");
        }
        user.setLastLoginAt(LocalDateTime.now());
        tenantUserMapper.updateById(user);
        TenantRole role = user.getRoleId() == null ? null : tenantRoleMapper.selectById(user.getRoleId());
        String token = jwtUtil.issue(user.getId(), user.getUsername(), "tenant", tenant.getId(), null, null);
        Map<String, Object> data = baseUser(user.getId(), user.getUsername(), user.getNickname(), "tenant",
                tenant.getId(), tenant.getName(), user.getOrgId());
        data.put("token", token);
        data.put("roles", List.of(role == null ? "企业管理员" : role.getName()));
        data.put("menus", role == null || role.getMenus() == null ? List.of("*") : parseList(role.getMenus()));
        data.put("buttons", role == null || role.getButtons() == null ? List.of("*") : parseList(role.getButtons()));
        data.put("tenantStatus", tenant.getStatus());
        data.put("tenantCode", tenant.getCode());
        data.put("seatLimit", tenant.getSeatLimit());
        data.put("seatUsed", tenant.getSeatUsed());
        return data;
    }

    private Map<String, Object> driverLogin(String username, String password) {
        if (!"1234".equals(password)) throw new BizException(401, "验证码错误（演示验证码 1234）");
        phoneLookupService.assertNotBlank(username);
        Driver driver = phoneLookupService.findDriverByPhone(username);
        if (driver == null) throw new BizException(404, "该手机号未绑定司机档案，请联系调度");
        if (!"在职".equals(driver.getStatus())) throw new BizException(403, "司机状态非在职，无法登录");
        String token = jwtUtil.issue(driver.getId(), driver.getName(), "driver", driver.getTenantId(), driver.getId(), driver.getCarrierId());
        Map<String, Object> data = baseUser(driver.getId(), username, driver.getName(), "driver", driver.getTenantId(), null, null);
        data.put("token", token);
        data.put("driverId", driver.getId());
        data.put("driverName", driver.getName());
        data.put("vehicleId", driver.getVehicleId());
        return data;
    }

    private Map<String, Object> carrierLogin(String username, String password) {
        if (!"1234".equals(password)) throw new BizException(401, "验证码错误（演示验证码 1234）");
        phoneLookupService.assertNotBlank(username);
        Carrier carrier = phoneLookupService.findCarrierByPhone(username);
        if (carrier == null) throw new BizException(404, "未找到承运商档案，请联系物流公司");
        String token = jwtUtil.issue(carrier.getId(), carrier.getName(), "carrier", carrier.getTenantId(), null, carrier.getId());
        Map<String, Object> data = baseUser(carrier.getId(), username, carrier.getName(), "carrier", carrier.getTenantId(), null, null);
        data.put("token", token);
        data.put("carrierId", carrier.getId());
        data.put("carrierName", carrier.getName());
        data.put("carrierStatus", carrier.getStatus());
        return data;
    }

    @Transactional
    public void changePassword(String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) throw new BizException("新密码至少 6 位");
        if ("platform".equals(TenantContext.scope())) {
            PlatformUser u = platformUserMapper.selectById(TenantContext.userId());
            if (u == null || !ENCODER.matches(oldPassword, u.getPassword())) throw new BizException("原密码不正确");
            u.setPassword(hash(newPassword));
            platformUserMapper.updateById(u);
        } else {
            TenantUser u = tenantUserMapper.selectById(TenantContext.userId());
            if (u == null || !ENCODER.matches(oldPassword, u.getPassword())) throw new BizException("原密码不正确");
            u.setPassword(hash(newPassword));
            tenantUserMapper.updateById(u);
        }
    }

    private Map<String, Object> baseUser(Long id, String username, String nickname, String scope,
                                         Long tenantId, String tenantName, Long orgId) {
        Map<String, Object> m = new LinkedHashMap<>();
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", id);
        user.put("username", username);
        user.put("nickname", nickname);
        user.put("scope", scope);
        user.put("tenantId", tenantId);
        user.put("tenantName", tenantName);
        user.put("orgId", orgId);
        m.put("user", user);
        return m;
    }

    private List<String> parseList(String json) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(json, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {
                    });
        } catch (Exception e) {
            return List.of();
        }
    }
}
