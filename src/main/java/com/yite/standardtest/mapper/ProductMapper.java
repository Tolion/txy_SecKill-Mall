package com.yite.standardtest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yite.standardtest.entity.ProductEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<ProductEntity> {
    // 分页返回所有商品
    // 继承 BaseMapper，selectPage/selectList 已经可用
}
