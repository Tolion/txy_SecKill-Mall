package com.yite.standardtest.mq;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yite.standardtest.DTO.SeckillMessageDTO;
import com.yite.standardtest.common.id.SnowflakeIdGenerator;
import com.yite.standardtest.common.sharding.SeckillOrderTableContext;
import com.yite.standardtest.config.RabbitMQConfig;
import com.yite.standardtest.entity.SeckillOrderEntity;
import com.yite.standardtest.entity.SeckillProductEntity;
import com.yite.standardtest.mapper.SeckillOrderMapper;
import com.yite.standardtest.mapper.SeckillProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.time.LocalDateTime;

@Service
public class SeckillOrderConsumer {

    private static final Logger log = LoggerFactory.getLogger(SeckillOrderConsumer.class);

    private final SeckillProductMapper seckillProductMapper;
    private final SeckillOrderMapper seckillOrderMapper;
    private final SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(1, 1);

    public SeckillOrderConsumer(SeckillProductMapper seckillProductMapper,
                                SeckillOrderMapper seckillOrderMapper) {
        this.seckillProductMapper = seckillProductMapper;
        this.seckillOrderMapper = seckillOrderMapper;
    }

    /**
     * 消费秒杀 MQ 消息。
     * 这里只负责把消息转换成秒杀订单并落库，不做业务校验。
     */
    @RabbitListener(queues = RabbitMQConfig.SECKILL_QUEUE, containerFactory = "rabbitListenerContainerFactory")
    public void onMessage(SeckillMessageDTO message) {
        if (message == null) {
            return;
        }
        process(message);
    }

    /**
     * 落库秒杀订单。
     * 流程：
     * 1. 从消息中取出用户、商品、数量等核心信息
     * 2. 设置分表上下文，确保 t_seckill_order 路由到正确分表
     * 3. 查询是否已存在相同用户 + 商品的秒杀订单
     * 4. 查询秒杀商品基础信息，校验商品仍然存在
     * 5. 组装完整秒杀订单并插入数据库
     * 6. 失败时回滚，结束后清理线程上下文
     */
    @Transactional
    public void process(SeckillMessageDTO message) {
        // 1. 提取消息中的关键信息
        Long userId = message.getUserId();
        Long seckillProductId = message.getSeckillProductId();
        Integer quantity = message.getQuantity() == null ? 1 : message.getQuantity();

        // 2. 设置分表上下文：后续对 t_seckill_order 的操作会自动路由到 userId 对应分表
        SeckillOrderTableContext.setUserId(userId);

        // 3. 订单时间：优先使用消息里的创建时间，没有则使用当前时间
        LocalDateTime now = message.getCreateTime() == null ? LocalDateTime.now() : message.getCreateTime();

        // 4. 幂等校验：相同用户、相同秒杀商品是否已经存在订单
        LambdaQueryWrapper<SeckillOrderEntity> query = new LambdaQueryWrapper<>();
        query.eq(SeckillOrderEntity::getUserId, userId)
                .eq(SeckillOrderEntity::getSeckillProductId, seckillProductId);
        SeckillOrderEntity existing = seckillOrderMapper.selectOne(query);
        if (existing != null) {
            return;
        }

        // 5. 查询秒杀商品，确认商品仍然有效
        SeckillProductEntity seckillProduct = seckillProductMapper.selectById(seckillProductId);
        if (seckillProduct == null) {
            throw new RuntimeException("秒杀商品不存在，seckillProductId=" + seckillProductId);
        }

        try {
            // 6. 组装完整秒杀订单实体
            SeckillOrderEntity seckillOrder = new SeckillOrderEntity();
            seckillOrder.setId(snowflakeIdGenerator.nextId());
            seckillOrder.setSecOrderNo(message.getSecOrderNo());
            seckillOrder.setUserId(userId);
            seckillOrder.setSeckillActivityId(message.getSeckillActivityId());
            seckillOrder.setSeckillProductId(seckillProductId);
            seckillOrder.setVersion(message.getVersion() == null ? 0 : message.getVersion());
            seckillOrder.setQuantity(quantity);
            seckillOrder.setSeckillUnitPrice(message.getSeckillUnitPrice());
            seckillOrder.setOriginUnitPrice(message.getOriginUnitPrice());
            seckillOrder.setDiscountAmount(message.getDiscountAmount());
            seckillOrder.setStatus(message.getStatus() == null ? 0 : message.getStatus());
            seckillOrder.setCreateTime(now);
            seckillOrder.setPayTime(message.getPayTime());
            seckillOrder.setCancelTime(message.getCancelTime());
            seckillOrder.setExpireTime(message.getExpireTime());
            seckillOrder.setUpdateTime(now);

            // 7. 插入分表后的秒杀订单表
            seckillOrderMapper.insert(seckillOrder);
        } catch (DuplicateKeyException ex) {
            // 8. 并发或重复消息时回滚事务，避免产生重复订单
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.info("秒杀幂等命中（重复消息/并发） userId={} seckillProductId={}", userId, seckillProductId);
        } finally {
            // 9. 无论成功失败都清理上下文，避免线程复用导致路由污染
            SeckillOrderTableContext.clear();
        }

        log.info("秒杀订单落库成功 userId={} seckillProductId={} secOrderNo={}", userId, seckillProductId, message.getSecOrderNo());
    }
}
