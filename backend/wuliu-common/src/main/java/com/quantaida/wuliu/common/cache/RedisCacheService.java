package com.quantaida.wuliu.common.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantaida.wuliu.common.context.TenantContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/** Redis 缓存实现：key 前缀 t:{tenantId}:，与内存实现行为一致。 */
@Service
@ConditionalOnProperty(name = "app.cache.type", havingValue = "redis")
public class RedisCacheService implements CacheService {
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public RedisCacheService(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    private String key(String key) {
        Long tenantId = TenantContext.tenantIdOrNull();
        return (tenantId == null ? "t:0:" : "t:" + tenantId + ":") + key;
    }

    @Override
    public Object get(String key) {
        String value = redis.opsForValue().get(key(key));
        if (value == null) return null;
        try {
            return objectMapper.readValue(value, Object.class);
        } catch (Exception e) {
            return value;
        }
    }

    @Override
    public void set(String key, Object value, long ttlSeconds) {
        try {
            String json = objectMapper.writeValueAsString(value);
            if (ttlSeconds > 0) {
                redis.opsForValue().set(key(key), json, Duration.ofSeconds(ttlSeconds));
            } else {
                redis.opsForValue().set(key(key), json);
            }
        } catch (Exception e) {
            throw new IllegalStateException("缓存写入失败", e);
        }
    }

    @Override
    public void delete(String key) {
        redis.delete(key(key));
    }

    @Override
    public void deleteByPrefix(String prefix) {
        var keys = redis.keys(key(prefix) + "*");
        if (keys != null && !keys.isEmpty()) redis.delete(keys);
    }
}
