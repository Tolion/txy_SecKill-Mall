package com.yite.standardtest.mq;

import com.yite.standardtest.DTO.SeckillMessageDTO;
import com.yite.standardtest.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class SeckillOrderDlqConsumer {

    private static final Logger log = LoggerFactory.getLogger(SeckillOrderDlqConsumer.class);

    private final StringRedisTemplate stringRedisTemplate;

    public SeckillOrderDlqConsumer(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * DLQ 最终失败补偿：把 Redis 库存与 buyer 标记“还原”。
     */
    @RabbitListener(queues = RabbitMQConfig.DEAD_LETTER_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void onMessage(SeckillMessageDTO message) {
        if (message == null) return;

        Long userId = message.getUserId();
        Long seckillProductId = message.getSeckillProductId();

        String stockKey = "seckill:stock:" + seckillProductId;
        String buyerKey = "seckill:buyer:" + seckillProductId;

        String luaScript =
                "redis.call('incr', KEYS[1]); " +
                "redis.call('srem', KEYS[2], ARGV[1]); " +
                "return 1;";

        RedisScript<Long> script = new DefaultRedisScript<>(luaScript, Long.class);
        List<String> keys = Arrays.asList(stockKey, buyerKey);
        try {
            stringRedisTemplate.execute(script, keys, userId.toString());
            log.warn("DLQ 补偿成功 userId={} seckillProductId={}", userId, seckillProductId);
        } catch (Exception e) {
            log.error("DLQ 补偿失败 userId={} seckillProductId={}", userId, seckillProductId, e);
        }
    }
}

