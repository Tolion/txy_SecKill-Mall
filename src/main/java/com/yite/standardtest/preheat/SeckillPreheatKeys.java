package com.yite.standardtest.preheat;

public final class SeckillPreheatKeys {

    private SeckillPreheatKeys() {
    }

    public static String stockKey(Long seckillProductId) {
        return "seckill:stock:" + seckillProductId;
    }

    public static String infoKey(Long seckillProductId) {
        return "seckill:info:" + seckillProductId;
    }

    public static String statusKey(Long seckillProductId) {
        return "seckill:status:" + seckillProductId;
    }

    public static String preheatDoneKey(Long seckillProductId) {
        return "seckill:preheat:done:" + seckillProductId;
    }
}
