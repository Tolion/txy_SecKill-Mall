package com.yite.standardtest.preheat2;

import com.yite.standardtest.preheat.SeckillPreheatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 应用启动后执行一次秒杀数据预热。
 *
 * 作用：
 * 1. 服务启动完成后，自动把数据库中的秒杀活动同步到 Redis
 * 2. 避免首次访问时临时加载数据导致延迟
 * 3. 为定时预热任务提供一个启动后的初始补齐动作
 */
@Component
@Order(10)
public class SeckillStartupPreheatRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SeckillStartupPreheatRunner.class);

    private final SeckillPreheatService seckillPreheatService;

    public SeckillStartupPreheatRunner(SeckillPreheatService seckillPreheatService) {
        this.seckillPreheatService = seckillPreheatService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            log.info("【TXY】开始执行秒杀启动预热任务...");
            seckillPreheatService.preheatUpcomingActivities();
            log.info("秒杀启动预热任务执行完成");
        } catch (Exception e) {
            log.error("秒杀启动预热任务执行失败", e);
        }
    }
}
