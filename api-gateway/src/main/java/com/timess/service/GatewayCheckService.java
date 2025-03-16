package com.timess.service;

import com.timess.apicommon.service.InnerUserInterfaceInfoService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Arrays;

/**
 * @author 33363
 */
@Service
public class GatewayCheckService {

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    // 预扣调用次数的原子脚本
    private static final String PRE_DEDUCT_SCRIPT =
            "local leftKey = KEYS[1] " +
                    "local preKey = KEYS[2] " +
                    "local required = tonumber(ARGV[1])\n" +
                    "local ttl = tonumber(ARGV[2])\n" +

                    // 检查剩余量（原子操作）
                    "local left = redis.call('get', leftKey) " +
                    "if not left then return {-1} end " +  // 需要初始化
                    "left = tonumber(left) " +

                    // 当剩余不足时直接拒绝
                    "if left < required then return {0} end " +

                    // 执行预扣操作
                    "redis.call('decrby', leftKey, required) " +
                    "redis.call('expire', leftKey, ttl) " +
                    "redis.call('incrby', preKey, required) " +
                    "redis.call('expire', preKey, ttl) " +
                    "return {1}";

    /**
     * 接口调用预扣（网关层）
     */
    public boolean preDeduct(Long userId, Long interfaceId) {
        String leftKey = buildLeftKey(userId, interfaceId);
        String preKey = buildPreKey(userId, interfaceId);

        // 1. 尝试原子预扣
        Long result = redisTemplate.execute(
                new DefaultRedisScript<>(PRE_DEDUCT_SCRIPT, Long.class),
                Arrays.asList(leftKey, preKey),
                "1"
        );

        // 2. 处理不同状态
        if (result == null) {
            return false;
        }
        if (result == -1) {
            return handleUninitialized(userId, interfaceId);
        }
        return result == 1;
    }

    /**
     * 处理初始化（带分布式锁）
     */
    private synchronized boolean handleUninitialized(Long userId, Long interfaceId) {
        String lockKey = buildLockKey(userId, interfaceId);
        try {
            // 1. 获取分布式锁
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(
                    lockKey, "1", Duration.ofSeconds(10)
            );
            if (!Boolean.TRUE.equals(locked)) {
                return false;
            }

            // 2. 双重检查
            String leftKey = buildLeftKey(userId, interfaceId);
            if (Boolean.TRUE.equals(redisTemplate.hasKey(leftKey))) {
                return preDeduct(userId, interfaceId);
            }

            // 3. 查询数据库
            Integer dbCount = innerUserInterfaceInfoService.leftNumCount(userId, interfaceId);
            if (dbCount == null || dbCount < 1) {
                // 设置保护性零值（5分钟过期）
                redisTemplate.opsForValue().set(
                        leftKey, "0", Duration.ofMinutes(5)
                );
                return false;
            }

            // 4. 初始化Redis
            redisTemplate.opsForValue().set(
                    leftKey,
                    String.valueOf(dbCount),
                    Duration.ofHours(48)
            );
            redisTemplate.opsForValue().set(
                    buildPreKey(userId, interfaceId),
                    "0",
                    Duration.ofHours(48)
            );
            return preDeduct(userId, interfaceId);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    // Key构建方法
    private String buildLeftKey(Long userId, Long interfaceId) {
        return String.format("count:left:%d:%d", userId, interfaceId);
    }

    private String buildPreKey(Long userId, Long interfaceId) {
        return String.format("count:pre:%d:%d", userId, interfaceId);
    }

    private String buildLockKey(Long userId, Long interfaceId) {
        return String.format("lock:count:%d:%d", userId, interfaceId);
    }
}