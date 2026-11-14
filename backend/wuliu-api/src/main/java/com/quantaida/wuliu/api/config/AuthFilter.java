package com.quantaida.wuliu.api.config;

import com.quantaida.wuliu.common.context.TenantContext;
import com.quantaida.wuliu.common.exception.BizException;
import com.quantaida.wuliu.common.security.IdempotencyService;
import com.quantaida.wuliu.common.security.JwtUtil;
import com.quantaida.wuliu.domain.entity.Tenant;
import com.quantaida.wuliu.domain.mapper.TenantMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * 鉴权与租户上下文过滤器：
 * 1) 解析 Bearer token，写入 TenantContext；2) 按路径前缀校验端类型；
 * 3) 无 token 的公开接口按 X-Tenant-Id/X-Tenant-Code 解析租户（默认 1）；
 * 4) 欠费/到期/停用租户的写操作直接拦截并返回中文提示。
 */
@Component
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {
    private static final List<String> PUBLIC = List.of(
            "/api/auth/login", "/api/public/", "/api/h5/", "/api/wechat/", "/api/files/",
            "/api/driver/login", "/api/carrier/login", "/api/health"
    );

    private final JwtUtil jwtUtil;
    private final TenantMapper tenantMapper;
    private final IdempotencyService idempotencyService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || path.startsWith("/actuator") || path.startsWith("/h2-console")) {
            chain.doFilter(request, response);
            return;
        }
        try {
            String header = request.getHeader("Authorization");
            boolean isPublic = PUBLIC.stream().anyMatch(path::startsWith);
            if (header != null && header.startsWith("Bearer ")) {
                Claims claims = jwtUtil.parse(header.substring(7));
                String scope = String.valueOf(claims.get("scope"));
                Long tenantId = claims.get("tenantId") == null ? null : ((Number) claims.get("tenantId")).longValue();
                Long driverId = claims.get("driverId") == null ? null : ((Number) claims.get("driverId")).longValue();
                Long carrierId = claims.get("carrierId") == null ? null : ((Number) claims.get("carrierId")).longValue();
                TenantContext.set(new TenantContext.Ctx(tenantId, Long.valueOf(claims.getSubject()),
                        String.valueOf(claims.get("username")), scope, driverId, carrierId, null));
                assertScope(path, scope);
                assertTenantWritable(request, tenantId);
            } else if (isPublic) {
                Long tenantId = resolvePublicTenant(request);
                TenantContext.set(new TenantContext.Ctx(tenantId, null, "访客", "public", null, null, null));
            } else {
                writeError(response, 401, "登录已失效，请重新登录");
                return;
            }
            if (!"GET".equalsIgnoreCase(request.getMethod())) {
                String idemKey = request.getHeader("X-Idempotency-Key");
                if (idemKey != null && !idemKey.isBlank()) {
                    idempotencyService.check(idemKey);
                }
            }
            chain.doFilter(request, response);
        } catch (BizException e) {
            writeError(response, e.getCode(), e.getMessage());
        } catch (Exception e) {
            writeError(response, 401, "登录状态校验失败，请重新登录");
        } finally {
            TenantContext.clear();
        }
    }

    private void assertScope(String path, String scope) {
        if (path.startsWith("/api/platform") && !"platform".equals(scope)) {
            throw BizException.forbidden("无权限访问平台运营端");
        }
        if (path.startsWith("/api/tenant") && !"tenant".equals(scope) && !"platform".equals(scope)) {
            throw BizException.forbidden("无权限访问租户后台");
        }
        if (path.startsWith("/api/driver") && !"driver".equals(scope)) {
            throw BizException.forbidden("请使用司机端账号登录");
        }
        if (path.startsWith("/api/carrier") && !"carrier".equals(scope)) {
            throw BizException.forbidden("请使用承运商账号登录");
        }
    }

    private void assertTenantWritable(HttpServletRequest request, Long tenantId) {
        if (tenantId == null || tenantId == 0 || "GET".equalsIgnoreCase(request.getMethod())) return;
        Tenant tenant = tenantMapper.selectById(tenantId);
        if (tenant == null) throw BizException.forbidden("租户不存在");
        if ("停用".equals(tenant.getStatus())) {
            throw BizException.forbidden("账号所属企业已停用，请联系服务商");
        }
        if ("已到期".equals(tenant.getStatus())) {
            throw BizException.forbidden("企业服务已到期，仅可查看与导出数据，请联系服务商续费");
        }
        if ("欠费".equals(tenant.getStatus())) {
            throw BizException.forbidden("企业已欠费，业务已暂停，请结清账单后操作");
        }
    }

    private Long resolvePublicTenant(HttpServletRequest request) {
        String tenantHeader = request.getHeader("X-Tenant-Id");
        if (tenantHeader != null) {
            try {
                return Long.parseLong(tenantHeader);
            } catch (NumberFormatException ignored) {
            }
        }
        String code = request.getHeader("X-Tenant-Code");
        if (code != null) {
            Tenant tenant = tenantMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Tenant>()
                    .eq(Tenant::getCode, code).last("limit 1"));
            if (tenant != null) return tenant.getId();
        }
        Tenant first = TenantContext.runWithoutTenant(() -> tenantMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Tenant>()
                        .orderByAsc(Tenant::getId).last("limit 1")));
        return first == null ? 1L : first.getId();
    }

    private void writeError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code == 429 ? 429 : code);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + code + ",\"message\":\"" + message.replace("\"", "'") + "\",\"data\":null}");
    }
}
