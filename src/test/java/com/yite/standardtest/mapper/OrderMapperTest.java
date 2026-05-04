package com.yite.standardtest.mapper;

import com.yite.standardtest.entity.OrderEntity;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@MapperScan("com.yite.standardtest.mapper")
class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;

    @Test
    @Transactional  // 测试结束自动回滚
    public void selectByOrderNo() {
        // 1. 构造订单对象
        OrderEntity order = new OrderEntity();
        order.setOrderNo("TangXinyi");
        order.setProductId(10L);
        order.setUserId(1L);
        order.setProductName("abc");
        order.setProductImg("aaa");
        order.setStatus(0); // 待支付
        order.setPayAmount(new BigDecimal("99.99"));
        order.setTradeNo("TANGXINYI");
        order.setCreateTime(LocalDateTime.now());
        order.setQuantity(1);
        order.setTotalPrice(BigDecimal.valueOf(99));
        order.setSource(0);

        // 2. 插入订单
        int rows = orderMapper.insert(order);
        assertEquals(1, rows, "插入应该成功");

        // 3. 根据 orderNo 查询
        String orderNo = order.getOrderNo(); // 你 Mapper 中 orderNo 是 String
        OrderEntity dbOrder = orderMapper.selectByOrderNo(orderNo);

        assertNotNull(dbOrder, "查询结果不应该为空");
        assertEquals(order.getUserId(), dbOrder.getUserId());
        assertEquals(order.getPayAmount(), dbOrder.getPayAmount());
        assertEquals(order.getStatus(), dbOrder.getStatus());

        System.out.println("查询订单成功：" + dbOrder);
    }
}