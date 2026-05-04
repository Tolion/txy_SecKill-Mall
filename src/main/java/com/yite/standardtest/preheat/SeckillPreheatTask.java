package com.yite.standardtest.preheat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SeckillPreheatTask {

    private static final Logger log = LoggerFactory.getLogger(SeckillPreheatTask.class);

    private final SeckillPreheatService seckillPreheatService;

    public SeckillPreheatTask(SeckillPreheatService seckillPreheatService) {
        this.seckillPreheatService = seckillPreheatService;
    }

    /**
     * 每 30 秒扫描一次即将开始的秒杀活动并执行预热。
     * 预热逻辑由 Redis 幂等标记控制，重复执行不会重复写入库存。
     */
    // @Scheduled(fixedDelay = 30000)
    public void run() {
        try {
            seckillPreheatService.preheatUpcomingActivities();
        } catch (Exception e) {
            log.error("秒杀预热任务执行失败", e);
        }
    }
}
