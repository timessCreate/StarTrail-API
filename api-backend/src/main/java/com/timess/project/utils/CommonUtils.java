package com.timess.project.utils;

import com.timess.project.model.entity.BatchDeductParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author 33363
 */
@Slf4j
@Component
public class CommonUtils {

    @Resource
    private  RedisTemplate<String, String> redisTemplate;

    // Key构建方法
    private static String buildLeftKey(Long userId, Long interfaceId) {
        return String.format("count:left:%d:%d", userId, interfaceId);
    }

    private static String buildPreKey(Long userId, Long interfaceId) {
        return String.format("count:pre:%d:%d", userId, interfaceId);
    }

    private static String buildLockKey(Long userId, Long interfaceId) {
        return String.format("lock:count:%d:%d", userId, interfaceId);
    }


    /**
     * 更新Redis未处理消息preKey数量
     */
    public static void cleanupRedisPreDeduct(List<BatchDeductParam> Params, RedisTemplate<String, String> redisTemplate) {
        Params.stream().forEach(param -> {
            try {
                String preKey = buildPreKey(param.getUserId(), param.getInterfaceId());
                //尝试进行原子减扣
                Long result = redisTemplate.execute(
                        new DefaultRedisScript<>(CONFIRM_SCRIPT, Long.class),
                        Collections.singletonList(preKey),
                        String.valueOf(param.getCount())
                );
            } catch (Exception e) {
                log.error("清理Redis预扣减失败: userId={}, interfaceId={}",
                        param.getUserId(), param.getInterfaceId(), e);
            }
        });
    }
    // 确认消费的原子脚本
//    private static final String CONFIRM_SCRIPT =
//            "local preKey = KEYS[1] " +
//                    "local consumed = tonumber(ARGV[1]) " +
//
//
//                    "local current = redis.call('decrby', preKey, consumed) " +
//
//
//                    "if current < 0 then " +
//                    "   redis.call('set', preKey, 0) " +
//                    "   return {-1} " +
//                    "end " +
//                    "return {1}";
//

    private static final String CONFIRM_SCRIPT =
            "local preKey = KEYS[1] " +
                    "local consumed = tonumber(ARGV[1]) " +
                    "if consumed == nil then " +
                    "   return -2 " +
                    "end " +
                    // 扣减预锁量
                    "local current = redis.call('decrby', preKey, consumed) " +
                    // 防止预锁量变为负数
                    "if current < 0 then " +
                    "   redis.call('set', preKey, 0) " +
                    "   return -1 " +
                    "end " +
                    "return 1";

    // 预锁键模式
    private static final String PRE_KEY_PATTERN = "count:pre:*";
    // 左键前缀
    private static final String LEFT_KEY_PREFIX = "count:left:";



    /**
     * 每日凌晨3点执行清理任务
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupZeroCounters() {
        List<String> keysToDelete = new ArrayList<>();

        // 使用Scan迭代避免阻塞
        try (Cursor<String> cursor = redisTemplate.scan(ScanOptions.scanOptions()
                .match(PRE_KEY_PATTERN)
                // 每次扫描1000个
                .count(1000)
                .build())) {

            while (cursor.hasNext()) {
                String preKey = cursor.next();
                String preValue = redisTemplate.opsForValue().get(preKey);

                // 检查预锁值是否为0
                if (Objects.equals(preValue, "0")) {
                    String leftKey = convertToLeftKey(preKey);
                    keysToDelete.add(preKey);
                    keysToDelete.add(leftKey);
                }
                // 保留非零Key并刷新TTL
                else {
                    redisTemplate.expire(preKey, 48, TimeUnit.HOURS);
                    redisTemplate.expire(convertToLeftKey(preKey), 48, TimeUnit.HOURS);
                    continue;
                }

                // 分批处理避免内存溢出
                if (keysToDelete.size() >= 500) {
                    executeBatchDelete(keysToDelete);
                    keysToDelete.clear();
                }
            }
        }
        // 处理剩余keys
        if (!keysToDelete.isEmpty()) {
            executeBatchDelete(keysToDelete);
        }
    }

    /**
     * 执行批量删除（原子操作）
     */
    private void executeBatchDelete(List<String> keys) {
        redisTemplate.execute(connection -> {
            connection.del(keys.toArray(new byte[0][]));
            return null;
        }, true);
    }

    /**
     * 转换preKey为leftKey
     */
    private String convertToLeftKey(String preKey) {
        String[] parts = preKey.split(":");
        // count:pre:userId:interfaceId -> count:left:userId:interfaceId
        return LEFT_KEY_PREFIX + parts[2] + ":" + parts[3];
    }

}
