package com.quantaida.wuliu.common.context;

import com.quantaida.wuliu.common.exception.BizException;

/**
 * 请求级上下文：租户、操作人、端类型。
 * 由鉴权过滤器写入、请求结束清理；业务层严禁从请求参数读取 tenantId。
 */
public final class TenantContext {
    private static final ThreadLocal<Ctx> HOLDER = new ThreadLocal<>();

    public record Ctx(Long tenantId, Long userId, String username, String scope,
                      Long driverId, Long carrierId, String tenantName) {
    }

    private TenantContext() {
    }

    public static void set(Ctx ctx) {
        HOLDER.set(ctx);
    }

    public static Ctx get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static Long tenantIdOrNull() {
        Ctx c = HOLDER.get();
        return c == null ? null : c.tenantId();
    }

    public static Long requireTenantId() {
        Ctx c = HOLDER.get();
        if (c == null || c.tenantId() == null || c.tenantId() == 0) {
            throw new BizException(403, "缺少租户上下文，请重新登录");
        }
        return c.tenantId();
    }

    public static Long userId() {
        Ctx c = HOLDER.get();
        return c == null ? null : c.userId();
    }

    public static String username() {
        Ctx c = HOLDER.get();
        return c == null ? "系统" : c.username();
    }

    public static String scope() {
        Ctx c = HOLDER.get();
        return c == null ? "" : c.scope();
    }

    public static boolean isPlatform() {
        return "platform".equals(scope());
    }

    public static boolean isTenant() {
        return "tenant".equals(scope());
    }

    public static Long driverId() {
        Ctx c = HOLDER.get();
        return c == null ? null : c.driverId();
    }

    public static Long carrierId() {
        Ctx c = HOLDER.get();
        return c == null ? null : c.carrierId();
    }

    /** 平台侧查询需临时关闭租户过滤时使用，务必成对调用。 */
    public static <T> T runWithoutTenant(java.util.function.Supplier<T> supplier) {
        Ctx old = HOLDER.get();
        HOLDER.set(new Ctx(null, old == null ? null : old.userId(),
                old == null ? "系统" : old.username(),
                old == null ? "platform" : "platform", null, null, null));
        try {
            return supplier.get();
        } finally {
            if (old == null) {
                HOLDER.remove();
            } else {
                HOLDER.set(old);
            }
        }
    }

    /** 定时任务按租户执行时使用。 */
    public static void runAs(Long tenantId, Runnable runnable) {
        Ctx old = HOLDER.get();
        HOLDER.set(new Ctx(tenantId, null, "系统任务", "system", null, null, null));
        try {
            runnable.run();
        } finally {
            if (old == null) {
                HOLDER.remove();
            } else {
                HOLDER.set(old);
            }
        }
    }

    /** 按租户执行并返回结果。 */
    public static <T> T runAsReturn(Long tenantId, java.util.function.Supplier<T> supplier) {
        Ctx old = HOLDER.get();
        HOLDER.set(new Ctx(tenantId, null, "系统任务", "system", null, null, null));
        try {
            return supplier.get();
        } finally {
            if (old == null) {
                HOLDER.remove();
            } else {
                HOLDER.set(old);
            }
        }
    }
}
