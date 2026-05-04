package com.yite.standardtest.preheat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yite.standardtest.entity.ProductEntity;
import com.yite.standardtest.entity.SeckillProductEntity;
import com.yite.standardtest.mapper.ProductMapper;
import com.yite.standardtest.mapper.SeckillProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillPreheatServiceImpl implements SeckillPreheatService {

    private static final Logger log = LoggerFactory.getLogger(SeckillPreheatServiceImpl.class);

    /**
     * 预热完成标记的过期时间。
     * 设为 24 小时，既能覆盖活动当天的重复扫描，也不会让幂等标记长期堆积。
     */
    private static final long PREHEAT_DONE_TTL_HOURS = 24L;

    private final SeckillProductMapper seckillProductMapper;
    private final ProductMapper productMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public SeckillPreheatServiceImpl(SeckillProductMapper seckillProductMapper,
                                     ProductMapper productMapper,
                                     StringRedisTemplate stringRedisTemplate) {
        this.seckillProductMapper = seckillProductMapper;
        this.productMapper = productMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 扫描即将开始的秒杀活动并执行预热。
     * 当前策略是：活动开始前 30 分钟进入预热窗口，窗口内的活动会被提前写入 Redis。
     */
    @Override
    public void preheatUpcomingActivities() {
        LocalDateTime now = LocalDateTime.now();

        // 预热窗口上界：当前时间往后推 30 分钟
        LocalDateTime upperBound = now.plusMinutes(30);

        // 查找满足预热窗口条件的秒杀商品：
        // 1. startTime 不为空
        // 2. endTime 不为空
        // 3. startTime 小于 upperBound（即将开始）
        // 4. endTime 大于 now（活动未过期）
        List<SeckillProductEntity> upcomingList = seckillProductMapper.selectList(
                new LambdaQueryWrapper<SeckillProductEntity>()
                        .isNotNull(SeckillProductEntity::getStartTime)
                        .isNotNull(SeckillProductEntity::getEndTime)
                        .lt(SeckillProductEntity::getStartTime, upperBound)
                        .gt(SeckillProductEntity::getEndTime, now)
        );

        // 对每个符合条件的活动执行一次预热
        for (SeckillProductEntity seckillProduct : upcomingList) {
            preheatOne(seckillProduct, now);
        }
    }

    /**
     * 预热单个秒杀活动。
     * 步骤：
     * 1. 检查活动是否已经预热过
     * 2. 读取普通商品信息
     * 3. 初始化库存、状态和活动摘要信息到 Redis
     * 4. 写入幂等标记，防止重复预热
     */
    private void preheatOne(SeckillProductEntity seckillProduct, LocalDateTime now) {
        Long seckillProductId = seckillProduct.getId();
        if (seckillProductId == null) {
            return;
        }

        // 幂等标记：如果已经存在，说明该活动已预热过，直接跳过
        String doneKey = SeckillPreheatKeys.preheatDoneKey(seckillProductId);
        Boolean alreadyDone = stringRedisTemplate.hasKey(doneKey);
        if (Boolean.TRUE.equals(alreadyDone)) {
            log.info("秒杀预热已存在，跳过 seckillProductId={}", seckillProductId);
            return;
        }

        // 读取普通商品信息，用于补充活动展示字段
        ProductEntity product = productMapper.selectById(seckillProduct.getProductId());
        if (product == null) {
            log.warn("秒杀预热失败：普通商品不存在 seckillProductId={}, productId={}", seckillProductId, seckillProduct.getProductId());
            return;
        }

        // 预热库存：优先使用剩余库存，没有则回退到总库存
        Integer stock = seckillProduct.getSeckillRemainStock() == null
                ? seckillProduct.getSeckillStock()
                : seckillProduct.getSeckillRemainStock();
        if (stock == null) {
            stock = 0;
        }

        // 1) 写入库存，供正式秒杀扣减使用
        stringRedisTemplate.opsForValue().set(SeckillPreheatKeys.stockKey(seckillProductId), String.valueOf(stock));

        // 2) 写入活动状态
        // 1 = 预热中，2 = 进行中，3 = 已结束
        stringRedisTemplate.opsForValue().set(SeckillPreheatKeys.statusKey(seckillProductId), String.valueOf(1));

        // 3) 写入活动摘要信息，使用 Hash 存储，便于后续按字段读取和扩展
        HashOperations<String, String, String> hashOps = stringRedisTemplate.opsForHash();
        String infoKey = SeckillPreheatKeys.infoKey(seckillProductId);
        hashOps.put(infoKey, "seckillProductId", String.valueOf(seckillProductId));
        hashOps.put(infoKey, "productId", String.valueOf(seckillProduct.getProductId()));
        hashOps.put(infoKey, "productName", nullToEmpty(product.getName()));
        hashOps.put(infoKey, "productImg", nullToEmpty(product.getImg()));
        hashOps.put(infoKey, "seckillPrice", nullToEmpty(seckillProduct.getSeckillPrice()));
        hashOps.put(infoKey, "stock", String.valueOf(stock));
        hashOps.put(infoKey, "remainStock", String.valueOf(stock));
        hashOps.put(infoKey, "perUserLimit", nullToEmpty(seckillProduct.getPerUserLimit()));
        hashOps.put(infoKey, "status", nullToEmpty(seckillProduct.getStatus()));
        hashOps.put(infoKey, "version", nullToEmpty(seckillProduct.getVersion()));
        hashOps.put(infoKey, "startTime", nullToEmpty(seckillProduct.getStartTime()));
        hashOps.put(infoKey, "endTime", nullToEmpty(seckillProduct.getEndTime()));
        hashOps.put(infoKey, "preheatTime", nullToEmpty(now));

        // 4) 写入幂等标记，防止定时任务重复预热同一活动
        stringRedisTemplate.opsForValue().set(
                doneKey,
                now.toString(),
                PREHEAT_DONE_TTL_HOURS,
                TimeUnit.HOURS
        );

        // 5) 让活动摘要信息与幂等标记同寿命，避免缓存长期残留
        stringRedisTemplate.expire(infoKey, Duration.ofHours(PREHEAT_DONE_TTL_HOURS));

        log.info("秒杀预热完成 seckillProductId={}, stock={}, startTime={}, endTime={}",
                seckillProductId, stock, seckillProduct.getStartTime(), seckillProduct.getEndTime());
    }

    /**
     * 将空值转换为空字符串，便于直接写入 Redis Hash。
     */
    private String nullToEmpty(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
