package com.persona.config;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单的内存令牌桶限流器。MVP 阶段用，后续可替换为 Redis + Bucket4j。
 */
@Component
public class RateLimiter {
    private final Map<String, long[]> buckets = new ConcurrentHashMap<>();
    private static final int AI_REQUESTS_PER_MINUTE = 5;

    /**
     * 检查是否允许通过。每 IP 每分钟最多 5 次 AI 请求。
     */
    public boolean tryAcquire(String key) {
        long now = System.currentTimeMillis();
        long windowStart = now - 60_000;

        long[] timestamps = buckets.computeIfAbsent(key, k -> new long[AI_REQUESTS_PER_MINUTE]);

        synchronized (timestamps) {
            int count = 0;
            long oldest = now;
            for (int i = 0; i < timestamps.length; i++) {
                if (timestamps[i] > windowStart) {
                    count++;
                    if (timestamps[i] < oldest) oldest = timestamps[i];
                }
            }
            if (count >= AI_REQUESTS_PER_MINUTE) {
                return false;
            }
            // 找空位写入
            for (int i = 0; i < timestamps.length; i++) {
                if (timestamps[i] <= windowStart) {
                    timestamps[i] = now;
                    return true;
                }
            }
            return false;
        }
    }

    /** 获取下次可用秒数（用于提示用户） */
    public long secondsUntilAvailable(String key) {
        long now = System.currentTimeMillis();
        long windowStart = now - 60_000;
        long[] timestamps = buckets.get(key);
        if (timestamps == null) return 0;
        synchronized (timestamps) {
            long oldest = now;
            for (long ts : timestamps) {
                if (ts > windowStart && ts < oldest) oldest = ts;
            }
            return Math.max(0, (oldest + 60_000 - now) / 1000 + 1);
        }
    }
}