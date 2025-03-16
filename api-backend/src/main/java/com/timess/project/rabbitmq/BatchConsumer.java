package com.timess.project.rabbitmq;

import com.rabbitmq.client.Channel;
import com.timess.apicommon.model.entity.UserInterfaceInfo;
import com.timess.apicommon.model.entity.entity.InvokeRecord;
import com.timess.project.mapper.UserInterfaceInfoMapper;
import com.timess.project.model.entity.BatchDeductParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.timess.project.utils.CommonUtils.cleanupRedisPreDeduct;

@Slf4j
@Service
public class BatchConsumer {

    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 批量处理扣减请求（带乐观锁）
     */
    @RabbitListener(queues = "invoke.main.queue", concurrency = "5")
    @Transactional(rollbackFor = Exception.class)
    public void handleBatch(List<InvokeRecord> batch, Channel channel) {
        // 1. 合并相同(userId, interfaceId)的扣减次数
        Map<CompositeKey, Integer> countMap = batch.stream()
                .collect(Collectors.groupingBy(
                        r -> new CompositeKey(r.getUserId(), r.getInterfaceInfoId()),
                        Collectors.summingInt(r -> 1)
                ));

        // 2. 批量查询当前版本号和剩余次数
        List<CompositeKey> keys = new ArrayList<>(countMap.keySet());
        List<UserInterfaceInfo> currentInfos = userInterfaceInfoMapper.batchSelectByCompositeKeys(keys);
        Map<CompositeKey, UserInterfaceInfo> infoMap = currentInfos.stream()
                .collect(Collectors.toMap(
                        info -> new CompositeKey(info.getUserId(), info.getInterfaceInfoId()),
                        info -> info
                ));

        // 3. 构造批量更新参数
        List<BatchDeductParam> params = countMap.entrySet().stream()
                .map(e -> {
                    UserInterfaceInfo info = infoMap.get(e.getKey());
                    if (info == null) {
                        log.error("数据不存在: userId={}, interfaceId={}",
                                e.getKey().userId, e.getKey().interfaceInfoId);
                        return null;
                    }
                    return new BatchDeductParam(
                            e.getKey().userId,
                            e.getKey().interfaceInfoId,
                            e.getValue(),
                            info.getVersion(),
                            info.getLeftNum()
                    );
                })
                .filter(Objects::nonNull)
                .filter(p -> p.getCurrentLeft() >= p.getCount())
                .collect(Collectors.toList());

        // 4. 执行批量更新
        userInterfaceInfoMapper.batchDeductWithVersion(params);

        // 5. 精确识别失败记录
        List<BatchDeductParam> failedParams = new ArrayList<>();
        List<BatchDeductParam> successParams = new ArrayList<>();

        // 重新查询数据库获取最新状态
        List<UserInterfaceInfo> latestInfos = userInterfaceInfoMapper.batchSelectByCompositeKeys(keys);
        Map<CompositeKey, UserInterfaceInfo> latestInfoMap = latestInfos.stream()
                .collect(Collectors.toMap(
                        info -> new CompositeKey(info.getUserId(), info.getInterfaceInfoId()),
                        info -> info
                ));

        params.forEach(param -> {
            UserInterfaceInfo latestInfo = latestInfoMap.get(new CompositeKey(param.getUserId(), param.getInterfaceId()));
            if (latestInfo == null) {
                failedParams.add(param);
                return;
            }
            // 检查版本号是否递增1，且剩余次数正确扣减
            if (latestInfo.getVersion() != param.getVersion() + 1 ||
                    latestInfo.getLeftNum() != param.getCurrentLeft() - param.getCount()) {
                failedParams.add(param);
            } else {
                successParams.add(param);
            }
        });

        // 6. 处理失败记录
        if (!failedParams.isEmpty()) {
            handlePartialFailure(failedParams);
        }

        // 7. 清理Redis预扣减（仅处理成功记录）
        cleanupRedisPreDeduct(successParams, redisTemplate);
    }

    /**
     * 处理部分失败记录（发送到重试队列）
     */
    private void handlePartialFailure(List<BatchDeductParam> failedParams) {
        failedParams.forEach(param -> {
            // 构造重试消息体（携带当前版本号）
            InvokeRecord record = new InvokeRecord(
                    param.getUserId(),
                    param.getInterfaceId()
            );
            rabbitTemplate.convertAndSend(
                    "invoke.retry.exchange",
                    "invoke.retry.routingKey",
                    record,
                    message -> {
                        message.getMessageProperties().setDelay(calculateRetryDelay(param));
                        return message;
                    }
            );
            log.warn("扣减失败进入重试: userId={}, interfaceId={}",
                    param.getUserId(), param.getInterfaceId());
        });
    }



    /**
     * 计算重试延迟时间（指数退避）
     */
    private int calculateRetryDelay(BatchDeductParam param) {
        String redisKey = String.format("retry:count:%d:%d",
                param.getUserId(), param.getInterfaceId());
        Long retryCount = redisTemplate.opsForValue().increment(redisKey, 1L);
        // 最大60秒
        return (int) Math.min(5000 * Math.pow(2, retryCount), 60000);
    }

    /**
     * 组合键类（确保实现equals和hashCode）
     */
    public static class CompositeKey {
        private final Long userId;
        private final Long interfaceInfoId;

        public CompositeKey(Long userId, Long interfaceInfoId) {
            this.userId = userId;
            this.interfaceInfoId = interfaceInfoId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            CompositeKey that = (CompositeKey) o;
            return Objects.equals(userId, that.userId) &&
                    Objects.equals(interfaceInfoId, that.interfaceInfoId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, interfaceInfoId);
        }
    }
}