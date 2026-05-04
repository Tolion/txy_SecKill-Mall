package com.yite.standardtest.config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

@Configuration
public class RabbitListenerConfig {

    /**
     * 关键：消费者抛异常时不进行 requeue（否则会反复重试导致无法进入 DLQ）。
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        // 让消息不要无限重回队列，而是依赖队列参数，把消息导向 DLQ（死信队列）
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}

