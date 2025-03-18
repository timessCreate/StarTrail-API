package com.timess.project.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    //主队列
    public static final String MAIN_QUEUE = "invoke.main.queue";
    //主交换机
    public static final String MAIN_EXCHANGE = "invoke.main.exchange";
    // 重试队列
    public static final String RETRY_QUEUE = "invoke.retry.queue";
    // 死信队列
    public static final String DLX_QUEUE = "invoke.dlx.queue";

    // 重试交换机
    public static final String RETRY_EXCHANGE = "invoke.retry.exchange";
    // 死信交换机
    public static final String DLX_EXCHANGE = "invoke.dlx.exchange";



    @Bean
    public Queue directQueue(){
        return new Queue(MAIN_QUEUE, true, false, false);
    }

    @Bean
    public DirectExchange directExchange(){
        return new DirectExchange(MAIN_EXCHANGE, true, false);
    }

    @Bean
    public Binding binding(@Qualifier("directQueue") Queue queue, DirectExchange directExchange){
        return BindingBuilder.bind(queue)
                .to(directExchange)
                .with("invoke.main.routingKey");
    }



    /**
     * 重试交换机（直连类型）
     */
    @Bean
    public DirectExchange retryExchange() {
        return new DirectExchange(RETRY_EXCHANGE);
    }
    /**
     * 重试队列（绑定到重试交换机）
     */
    @Bean
    public Queue retryQueue() {
        return QueueBuilder.durable(RETRY_QUEUE)
                // 设置死信交换机（用于转发超过重试次数的消息到死信队列）
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DLX_QUEUE)
                // 设置消息过期时间（例如两个小时后进入死信队列）
                .withArgument("x-message-ttl", 60000 * 120)
                .build();
    }

    /**
     * 绑定重试队列到重试交换机
     */
    @Bean
    public Binding retryBinding() {
        return BindingBuilder.bind(retryQueue())
                .to(retryExchange())
                .with(RETRY_QUEUE);
    }

    /**
     * 死信队列（绑定到死信交换机）
     */
    @Bean
    public Queue dlxQueue() {
        return new Queue(DLX_QUEUE, true);
    }

    /**
     * 死信交换机（直连类型）
     */
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE);
    }
    /**
     * 绑定死信队列到死信交换机
     */
    @Bean
    public Binding dlxBinding() {
        return BindingBuilder.bind(dlxQueue())
                .to(dlxExchange())
                .with(DLX_QUEUE);
    }

    @Bean(name = "batchContainerFactory")
    public SimpleRabbitListenerContainerFactory batchContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setBatchListener(true);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(1);
        factory.setConsumerBatchEnabled(true);
        factory.setBatchSize(100);
        factory.setPrefetchCount(100);
        // 配置JSON消息转换器（反序列化消息体）
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        return factory;
    }

}