package com.yite.standardtest.VO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SeckillProductVO {
    private Long seckillProductId;
    private String seckillProductName;
    private BigDecimal seckillProductPrice;
    private String seckillProductImg;
    private Integer seckillProductQuantity;
    private LocalDateTime seckillProductStartTime;
    private LocalDateTime seckillProductEndTime;
}