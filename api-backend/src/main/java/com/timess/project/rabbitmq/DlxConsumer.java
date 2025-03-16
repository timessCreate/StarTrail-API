package com.timess.project.rabbitmq;

import com.timess.project.config.RabbitMqConfig;
import com.timess.project.model.entity.BatchDeductParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DlxConsumer {

    /**
     * 监听死信队列
     */
    @RabbitListener(queues = RabbitMqConfig.DLX_QUEUE)
    public void handleDlx(BatchDeductParam batchDeductParam) {
        log.error("最终扣减失败，需人工处理: userId={}, interfaceId={}",
                batchDeductParam.getUserId(), batchDeductParam.getInterfaceId());
        // TODO: 发送报警邮件或记录到数据库
    }
}