package com.timess.project.rabbitmq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.timess.apicommon.model.entity.UserInterfaceInfo;
import com.timess.project.config.RabbitMqConfig;
import com.timess.project.mapper.UserInterfaceInfoMapper;
import com.timess.project.model.entity.BatchDeductParam;
import com.timess.project.service.UserInterfaceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;

import static com.timess.project.utils.CommonUtils.cleanupRedisPreDeduct;

@Slf4j
@Service
public class RetryConsumer {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;
    /**
     * 监听重试队列（带3次重试限制）
     */
    @RabbitListener(
        queues = RabbitMqConfig.RETRY_QUEUE
    )
    @Transactional(rollbackFor = Exception.class)
    public void handleRetry(BatchDeductParam batchDeductParam, Message message) {
        // 1. 获取重试次数
        Integer retryCount = (Integer) message.getMessageProperties()
                .getHeaders().getOrDefault("retry-count", 0);

        // 2. 超过最大重试次数则转入死信队列
        if (retryCount >= 3) {
            log.error("重试次数超限，转入死信队列: userId={}, interfaceId={}",
                    batchDeductParam.getUserId(), batchDeductParam.getInterfaceId());
            // 手动发送到死信队列（无需设置延迟）
            rabbitTemplate.convertAndSend(
                    // 死信交换机名称
                    RabbitMqConfig.DLX_EXCHANGE,
                    // 死信队列路由键
                    RabbitMqConfig.DLX_QUEUE,
                    batchDeductParam
            );
        }
        // 3. 查询当前版本号
        LambdaQueryWrapper<UserInterfaceInfo> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(UserInterfaceInfo::getUserId, batchDeductParam.getUserId());
        lambdaQueryWrapper.eq(UserInterfaceInfo::getInterfaceInfoId, batchDeductParam.getInterfaceId());
        UserInterfaceInfo info = userInterfaceInfoService.getOne(lambdaQueryWrapper);
        if (info == null) {
            log.error("数据不存在，终止重试: userId={}, interfaceId={}",
                batchDeductParam.getUserId(), batchDeductParam.getInterfaceId());
            return;
        }

        // 4. 构造扣减参数（使用消息中的版本号）
        BatchDeductParam param = new BatchDeductParam(
                batchDeductParam.getUserId(),
                batchDeductParam.getInterfaceId(),
                batchDeductParam.getCount(),
                info.getVersion(),
                info.getLeftNum()
        );
        // 5. 执行扣减
        int success = userInterfaceInfoMapper.batchDeductWithVersion(Collections.singletonList(param));

        // 6. 处理失败
        if (success == 0) {
            // 更新重试次数并重新入队
            message.getMessageProperties().setHeader("retry-count", retryCount + 1);
            rabbitTemplate.convertAndSend(
                    // 重试交换机
                    RabbitMqConfig.RETRY_EXCHANGE,
                    // 重试队列路由键
                    RabbitMqConfig.RETRY_QUEUE,
                    batchDeductParam
            );
            throw new AmqpRejectAndDontRequeueException("Retry needed");
        }
        // 7. 重新更正redis数据
        ArrayList<BatchDeductParam> batchDeductParams = new ArrayList<>();
        batchDeductParams.add(param);
        cleanupRedisPreDeduct(batchDeductParams, redisTemplate);
    }


}