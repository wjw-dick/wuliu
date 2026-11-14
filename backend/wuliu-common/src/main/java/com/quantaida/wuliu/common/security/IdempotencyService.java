package com.quantaida.wuliu.common.security;

import com.quantaida.wuliu.common.context.TenantContext;
import com.quantaida.wuliu.common.exception.BizException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 幂等键：同一租户/用户 10 分钟内相同幂等键重复提交直接拦截。 */
@Service
public class IdempotencyService {
    private final Map<String, Long> keys = new ConcurrentHashMap<>();
    private static final long TTL = 10 * 60 * 1000L;

    public void check(String key) {
        if (key == null || key.isBlank()) return;
        long now = System.currentTimeMillis();
        keys.entrySet().removeIf(e -> e.getValue() < now);
        String full = TenantContext.tenantIdOrNull() + ":" + TenantContext.userId() + ":" + key;
        Long exist = keys.putIfAbsent(full, now + TTL);
        if (exist != null) {
            throw BizException.conflict("请勿重复提交，本次请求已处理或正在处理中");
        }
    }

    public void remove(String key) {
        if (key != null) keys.remove(TenantContext.tenantIdOrNull() + ":" + TenantContext.userId() + ":" + key);
    }
}
