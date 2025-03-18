package com.timess.service;

import com.timess.apicommon.service.InnerUserInterfaceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 33363
 */
@Service
@Slf4j
public class GatewayCheckService {


    @Autowired
    private StringRedisTemplate redisTemplate;

    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    // 预扣调用次数的原子脚本
    private static final String PRE_DEDUCT_SCRIPT =
            "local leftKey = KEYS[1]\n" +
                    "local preKey = KEYS[2]\n" +
                    "local required = tonumber(ARGV[1])\n" +
                    "local ttl = tonumber(ARGV[2])\n" +
                    "\n" +
                    "-- 参数校验：确保 required 和 ttl 是合法数字\n" +
                    "if not required or not ttl then\n" +
                    "    return {-2}  -- 参数错误\n" +
                    "end\n" +
                    "\n" +
                    "local left = redis.call('GET', leftKey)\n" +
                    "if not left then\n" +
                    "    return {-1}  -- 未初始化\n" +
                    "end\n" +
                    "\n" +
                    "left = tonumber(left)\n" +
                    "if not left then\n" +
                    "    return {-3}  -- 值类型非法\n" +
                    "end\n" +
                    "\n" +
                    "if left < required then\n" +
                    "    return {0}   -- 剩余不足\n" +
                    "end\n" +
                    "\n" +
                    "-- 执行预扣逻辑\n" +
                    "redis.call('DECRBY', leftKey, required)\n" +
                    "redis.call('EXPIRE', leftKey, ttl)\n" +
                    "redis.call('INCRBY', preKey, required)\n" +
                    "redis.call('EXPIRE', preKey, ttl)\n" +
                    "return {1}       -- 成功";

    /**
     * 接口调用预扣（网关层）
     */
    public boolean preDeduct(Long userId, Long interfaceId) {
        String leftKey = buildLeftKey(userId, interfaceId);
        String preKey = buildPreKey(userId, interfaceId);
        boolean sign = false;
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(PRE_DEDUCT_SCRIPT, Long.class);
        script.setScriptText(PRE_DEDUCT_SCRIPT);
        try {
            Long result = redisTemplate.execute(
                    script,
                    Arrays.asList(leftKey, preKey),
                    "1",
                    "172800"
            );

            if (result == null) {
                log.error("预扣返回空值，userId: {}, interfaceId: {}", userId, interfaceId);
                return false;
            }
            if (result == -1) {
                 return handleUninitialized(userId, interfaceId);
            }
            if (result == -2 || result == -3) {
                log.error("预扣参数或数据异常，userId: {}, interfaceId: {}", userId, interfaceId);
                return false;
            }
            return result == 1;
        } catch (RedisSystemException e) {
            log.error("Redis 预扣异常，userId: {}, interfaceId: {}", userId, interfaceId, e);
            return false;
        }
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