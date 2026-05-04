package com.yite.standardtest.service.impl;

import com.yite.standardtest.DTO.ProductPayDTO;
import com.yite.standardtest.VO.ProductPayVO;
import com.yite.standardtest.common.security.context.LoginUser;
import com.yite.standardtest.common.security.context.LoginUserContext;
import com.yite.standardtest.entity.OrderEntity;
import com.yite.standardtest.entity.ProductEntity;
import com.yite.standardtest.mapper.OrderMapper;
import com.yite.standardtest.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
public class PayServiceImpl implements PayService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    @Transactional
    public ProductPayVO payProduct(ProductPayDTO dto){

        // 获取 订单号
        String orderNo = dto.getOrderNo();

        // 获取用户 id
        LoginUser user = LoginUserContext.get();
        if (user == null) {
            throw new RuntimeException("用户未登录");
        }
        Long userId = user.getUserId();

        // 1. 查询订单
        OrderEntity order = orderMapper.selectByOrderNo(orderNo);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在，或不属于当前用户");
        }

        // 4. 检查支付状态
        if (order.getStatus() != null && order.getStatus() == 2) {
            throw new RuntimeException("订单已支付");
        }

        //order.setOrderNo(orderNo);
        //order.setUserId(userId);
        //order.setProductId(product.getId());
        //order.setProductName(product.getName());
        //order.setProductImg(product.getImg());
        //order.setQuantity(quantity);
        //order.setTotalPrice(totalPrice);
        //order.setPayAmount(totalPrice); // 普通订单实际支付 = 总价
        ///order.setSource(1); // 1=普通订单
        //order.setStatus(0); // 0=待支付
        //order.setCreateTime(LocalDateTime.now());

        // 5. 模拟生成支付流水号
        String tradeNo = "MOCK" + System.currentTimeMillis();

        // 6. 更新订单状态
        order.setStatus(2);                  // 2=已支付
        order.setPayTime(LocalDateTime.now());
        order.setTradeNo(tradeNo);
        orderMapper.updateById(order);

        // 7. 返回 VO 给前端
        ProductPayVO vo = new ProductPayVO();
        vo.setOrderNo(orderNo);
        vo.setPayStatus(2);                     // 2=已支付
        vo.setPayAmount(order.getPayAmount());    // 实际支付金额
        vo.setPayTime(LocalDateTime.now());
        vo.setTradeNo(tradeNo);
        return vo;
    }

    // 真实支付回调（签名校验）
    public void realNotify(Map<String, String> params) {
        // TODO: 验证签名、金额、幂等处理
    }

}
