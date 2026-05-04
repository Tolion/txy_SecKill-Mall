package com.yite.standardtest.common.sharding;

import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;

public class SeckillOrderTableNameHandler implements TableNameHandler {

    private final String baseTableName;

    public SeckillOrderTableNameHandler(String baseTableName) {
        this.baseTableName = baseTableName;
    }

    @Override
    public String dynamicTableName(String sql, String tableName) {
        if (!baseTableName.equalsIgnoreCase(tableName)) {
            return tableName;
        }
        Long userId = SeckillOrderTableContext.getUserId();
        return SeckillOrderTableContext.resolveTable(baseTableName, userId);
    }
}