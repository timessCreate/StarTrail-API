package com.timess.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class RabbitConfig implements RabbitTemplate.ReturnsCallback, RabbitTemplate.ConfirmCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${rabbitconfig.queue.name}")
    private String queueName;

    @Value("${rabbitconfig.exchange.name}")
    private String exchangeName;

    @Value("${rabbitconfig.routingkey.name}")
    private String routingKeyName;

    @PostConstruct
    public void initRabbitTemplate(){
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnsCallback(this);
        System.out.println("======queueName:" + queueName);
        System.out.println("======exchangeName:" + exchangeName);
        System.out.println("======routingKeyName:" + routingKeyName);
    }
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {

    }

    @Override
    public void returnedMessage(ReturnedMessage returnedMessage) {
        log.info("消息主体： " + new String(returnedMessage.getMessage().getBody()));
        log.info("应答码：" + returnedMessage.getReplyCode());
        log.info("描述：" + returnedMessage.getReplyText());
        log.info("该消息使用的交换机：" + returnedMessage.getExchange());
        log.info("该消息使用的路由键：" + returnedMessage.getRoutingKey());
    }

    @Bean
    public Queue directQueue(){
        return new Queue(queueName, true, false, false);
    }

    @Bean
    public DirectExchange directExchange(){
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public Binding binding(Queue queue, DirectExchange directExchange){
        return BindingBuilder.bind(queue)
                .to(directExchange)
                .with(routingKeyName);
    }
}
