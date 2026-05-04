package com.yite.standardtest.common.sharding;

public final class SeckillOrderTableContext {

    private static final int SHARD_COUNT = 4;
    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    private SeckillOrderTableContext() {
    }

    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    public static void clear() {
        USER_ID_HOLDER.remove();
    }

    public static int shardIndex(Long userId) {
        if (userId == null) {
            return 0;
        }
        return Math.floorMod(userId.intValue(), SHARD_COUNT);
    }

    public static String resolveTable(String baseTableName, Long userId) {
        return baseTableName + "_" + shardIndex(userId);
    }
}