package com.yite.standardtest.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    // ========== 常量定义 ==========
    
    // 主交换机名称
    public static final String SECKILL_EXCHANGE = "seckill.exchange";
    
    // 主队列名称
    public static final String SECKILL_QUEUE = "seckill.queue";
    
    // 死信队列名称
    public static final String DEAD_LETTER_QUEUE = "seckill.dlx.queue";
    
    // 死信交换机名称
    public static final String DEAD_LETTER_EXCHANGE = "seckill.dlx.exchange";
    
    // 路由键
    public static final String ROUTING_KEY = "seckill.order";
    public static final String DEAD_LETTER_ROUTING_KEY = "seckill.dead";
    
    // 重试相关配置
    public static final int MAX_RETRY_COUNT = 3;
    public static final long MESSAGE_TTL = 60000; // 60秒
    public static final long DLQ_MESSAGE_TTL = 86400000; // 死信队列消息24小时过期

    @Bean
    public MessageConverter jacksonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        // 关键：让 Jackson 能序列化/反序列化 LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jacksonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jacksonMessageConverter);
        return rabbitTemplate;
    }


    /**
     * 声明主队列，并配置死信交换机、死信路由键、消息TTL等
     */
    @Bean
    public Queue seckillQueue() {
        Map<String, Object> arguments = new HashMap<>();
        
        // 1. 配置死信交换机
        arguments.put("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE);
        
        // 2. 配置死信路由键（消息进入死信队列时使用的路由键）
        arguments.put("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY);
        
        // 3. 配置队列级别的TTL（消息在队列中的存活时间）
        arguments.put("x-message-ttl", MESSAGE_TTL);
        
        // 4. 配置队列最大长度（防止消息堆积过多）
        arguments.put("x-max-length", 10000);
        
        // 5. 配置溢出策略（reject-publish：溢出时拒绝新消息，避免内存爆炸）
        arguments.put("x-overflow", "reject-publish");
        
        // 6. 开启队列持久化
        return QueueBuilder.durable(SECKILL_QUEUE)
                .withArguments(arguments)
                .build();
    }


    /**
     * 声明死信队列
     * 死信队列本身也可以配置TTL，防止消息无限堆积
     */
    @Bean
    public Queue deadLetterQueue() {
        Map<String, Object> arguments = new HashMap<>();
        
        // 死信队列也可以配置TTL（消息在死信队列中的存活时间）
        arguments.put("x-message-ttl", DLQ_MESSAGE_TTL);
        
        // 设置队列最大长度
        arguments.put("x-max-length", 5000);
        
        return QueueBuilder.durable(DEAD_LETTER_QUEUE)
                .withArguments(arguments)
                .build();
    }


    /**
     * 声明主交换机（使用Topic类型，支持通配符路由）
     * 也可以使用Direct或Fanout，根据业务需求选择
     */
    @Bean
    public TopicExchange seckillExchange() {
        return ExchangeBuilder.topicExchange(SECKILL_EXCHANGE)
                .durable(true)        // 持久化
                .build();
    }

    /**
     * 声明死信交换机（使用Direct类型，精确匹配）
     */
    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder.directExchange(DEAD_LETTER_EXCHANGE)
                .durable(true)
                .build();
    }


    /**
     * 主队列与主交换机的绑定
     */
    @Bean
    public Binding bindingSeckillQueueToExchange(
            @Qualifier("seckillQueue") Queue queue,
            @Qualifier("seckillExchange") TopicExchange exchange) {
        
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(ROUTING_KEY);
    }


    /**
     * 死信队列与死信交换机的绑定
     */
    @Bean
    public Binding bindingDeadLetterQueueToExchange(
            @Qualifier("deadLetterQueue") Queue queue,
            @Qualifier("deadLetterExchange") DirectExchange exchange) {
        
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(DEAD_LETTER_ROUTING_KEY);
    }
}
