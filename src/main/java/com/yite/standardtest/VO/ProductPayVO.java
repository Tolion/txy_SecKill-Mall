package com.yite.standardtest.VO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductPayVO {
    private String orderNo;
    private Integer payStatus;      // 0=未支付 1=支付中 2=已支付 3=失败
    private BigDecimal payAmount;   // 实际支付金额
    private LocalDateTime payTime;
    private String tradeNo;         // 第三方流水号
}
