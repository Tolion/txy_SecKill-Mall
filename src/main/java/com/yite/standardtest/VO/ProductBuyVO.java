package com.yite.standardtest.VO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductBuyVO {
    private String userName;        // 用户名

    private String orderNo;          // 订单号（最关键）
    private String productName;      // 商品名
    private String productImg;       // 商品图片

    private BigDecimal singlePrice;  // 单价
    private Integer quantity;        // 数量
    private BigDecimal totalPrice;   // 总价

    private Integer payStatus;       // 0=未支付
    private LocalDateTime createTime;   // 订单创建时间
}
