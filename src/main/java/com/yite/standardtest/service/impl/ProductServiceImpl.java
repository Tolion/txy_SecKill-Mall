package com.yite.standardtest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yite.standardtest.DTO.ProductBuyDTO;
import com.yite.standardtest.DTO.ProductPayDTO;
import com.yite.standardtest.VO.ProductBuyVO;
import com.yite.standardtest.VO.ProductPayVO;
import com.yite.standardtest.common.security.context.LoginUser;
import com.yite.standardtest.common.security.context.LoginUserContext;
import com.yite.standardtest.common.id.SnowflakeIdGenerator;
import com.yite.standardtest.controller.ProductsController;
import com.yite.standardtest.entity.OrderEntity;
import com.yite.standardtest.entity.ProductEntity;
import com.yite.standardtest.mapper.OrderMapper;
import com.yite.standardtest.mapper.ProductMapper;
import com.yite.standardtest.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    private final SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(1, 1);

    private static final Logger log = LoggerFactory.getLogger(ProductsController.class);
    private static final DateTimeFormatter ORDER_NO_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");


    @Override
    public Page<ProductEntity> pageProductList(int page, int size) {

        log.info("pagesize: " + size);

        // 1. 构建分页对象
        Page<ProductEntity> pageParam = new Page<>(page, size);

        // 2. 构建查询条件（先空着，后面可扩展）
        LambdaQueryWrapper<ProductEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(ProductEntity::getCreateTime);

        // 3. 执行分页查询
        productMapper.selectPage(pageParam, queryWrapper);

        // 4. 直接返回 Page（MP 已经帮你填充好了）
        return pageParam;

    }

    @Override
    public ProductBuyVO buyProduct(ProductBuyDTO dto){

        // 获取商品 id，和商品数量
        Long productId = dto.getProductId();
        Integer quantity = dto.getQuantity();

        // 获取用户 id
        LoginUser user = LoginUserContext.get();
        Long userId = user.getUserId();

        // 1. 查询商品
        ProductEntity product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }

        // 2. 计算总价
        BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(quantity));

        // 3. 创建订单实体
        OrderEntity order = new OrderEntity();
        order.setId(snowflakeIdGenerator.nextId());
        order.setUserId(userId);
        order.setProductId(product.getId());
        order.setProductName(product.getName());
        order.setProductImg(product.getImg());
        order.setQuantity(quantity);
        order.setTotalPrice(totalPrice);
        order.setPayAmount(totalPrice); // 普通订单实际支付 = 总价
        order.setStatus(0); // 0=待支付
        order.setCreateTime(LocalDateTime.now());

        // 4. 订单号：ORD + 时间戳 + 用户id + 雪花ID
        String orderNo = "ORD" + order.getCreateTime().format(ORDER_NO_TIME_FORMATTER) + userId + order.getId();
        order.setOrderNo(orderNo);

        // 5. 直接插入普通订单表 t_order（不分表）
        orderMapper.insert(order);
        log.info("TXY - order.toString(): " +order.toString());

        // 6. 返回 VO 给前端
        ProductBuyVO vo = new ProductBuyVO();
        vo.setUserName(user.getUsername());
        vo.setOrderNo(orderNo);             // 订单号（最关键）
        vo.setProductName(product.getName());
        vo.setProductImg(product.getImg());
        vo.setSinglePrice(product.getPrice());
        vo.setQuantity(quantity);
        vo.setTotalPrice(totalPrice);
        vo.setPayStatus(0);                 // 0=未支付
        vo.setCreateTime(LocalDateTime.now());  // 订单创建时间
        return vo;

    }
}
