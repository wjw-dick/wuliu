package com.quantaida.wuliu.common.cache;

import com.quantaida.wuliu.common.context.TenantContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 演示/单机默认实现；生产切 app.cache.type=redis 使用 RedisCacheService。 */
@Service
@ConditionalOnProperty(name = "app.cache.type", havingValue = "memory", matchIfMissing = true)
public class MemoryCacheService implements CacheService {
    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    private record Entry(Object value, long expireAt) {
    }

    private String key(String key) {
        Long tenantId = TenantContext.tenantIdOrNull();
        return (tenantId == null ? "t:0:" : "t:" + tenantId + ":") + key;
    }

    @Override
    public Object get(String key) {
        Entry e = store.get(key(key));
        if (e == null) return null;
        if (e.expireAt() > 0 && e.expireAt() < System.currentTimeMillis()) {
            store.remove(key(key));
            return null;
        }
        return e.value();
    }

    @Override
    public void set(String key, Object value, long ttlSeconds) {
        long expireAt = ttlSeconds <= 0 ? 0 : System.currentTimeMillis() + ttlSeconds * 1000;
        store.put(key(key), new Entry(value, expireAt));
    }

    @Override
    public void delete(String key) {
        store.remove(key(key));
    }

    @Override
    public void deleteByPrefix(String prefix) {
        String full = key(prefix);
        store.keySet().removeIf(k -> k.startsWith(full));
    }
}
