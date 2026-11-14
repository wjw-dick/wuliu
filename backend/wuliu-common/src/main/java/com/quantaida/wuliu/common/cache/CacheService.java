package com.quantaida.wuliu.common.cache;

/** 缓存抽象：实现可切换本地内存 / Redis，key 统一带 t:{tenantId}: 前缀。 */
public interface CacheService {
    Object get(String key);

    void set(String key, Object value, long ttlSeconds);

    void delete(String key);

    void deleteByPrefix(String prefix);
}
