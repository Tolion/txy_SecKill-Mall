package com.yite.standardtest.MQ;

import com.yite.standardtest.DTO.SeckillMessageDTO;
import com.yite.standardtest.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeckillOrderProducer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendSeckillOrder(SeckillMessageDTO message) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.SECKILL_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                message
        );
    }
}

