package com.timess.manager;

/**
 * @author 33363
 */

import com.timess.apicommon.model.entity.entity.InvokeRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.Duration;

@Slf4j
@Component
public class AsyncInvokeManager {

    @Resource
    RabbitTemplate rabbitTemplate;

    @Value("${rabbitconfig.routingkey.name}")
    String routingKey;

    @Value("${rabbitconfig.exchange.name}")
    private String exchangeName;

    @Autowired
    private StringRedisTemplate redisTemplate;
    public void addInvokeRecord(String requestId, Long interfaceId, Long userId) {
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(
                "uuid:req:" + requestId,
                "processed",
                Duration.ofMinutes(5)
        );
        if(Boolean.TRUE.equals(isNew)){
            rabbitTemplate.convertAndSend(
                    // 显式指定交换机名称
                    exchangeName,
                    // 使用正确的路由键
                    routingKey,
                    new InvokeRecord(interfaceId, userId),
                    message -> {
                        message.getMessageProperties()
                                .setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return message;
                    }
            );
        }
    }
}
