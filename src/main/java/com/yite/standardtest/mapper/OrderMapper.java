package com.yite.standardtest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yite.standardtest.entity.OrderEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {
    // 新增订单，BaseMapper 自带 insert 方法
    // orderMapper.insert(order);

    // 根据 订单号，而不是 订单Id，查询订单。
    OrderEntity selectByOrderNo(@Param("orderNo") String orderNo);
}
