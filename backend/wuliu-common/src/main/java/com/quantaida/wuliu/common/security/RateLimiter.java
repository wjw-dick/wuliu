package com.quantaida.wuliu.common.security;

import com.quantaida.wuliu.common.exception.BizException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** 轻量限流（滑动窗口计数），用于询价、下单、轨迹上报等高频接口。 */
@Service
public class RateLimiter {
    private record Bucket(long windowStart, int count) {
    }

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public void acquire(String key, int limit, int seconds) {
        long now = System.currentTimeMillis();
        long window = seconds * 1000L;
        Bucket b = buckets.compute(key, (k, old) -> {
            if (old == null || now - old.windowStart() >= window) return new Bucket(now, 1);
            return new Bucket(old.windowStart(), old.count() + 1);
        });
        if (b.count() > limit) {
            throw new BizException(429, "操作过于频繁，请稍后再试");
        }
        if (buckets.size() > 20000) {
            buckets.entrySet().removeIf(e -> now - e.getValue().windowStart() > window);
        }
    }
}
