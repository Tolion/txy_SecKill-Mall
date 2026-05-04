package com.yite.standardtest.preheat;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SeckillPreheatPayload {
    private Long seckillProductId;
    private Long productId;
    private String productName;
    private String productImg;
    private BigDecimal seckillPrice;
    private Integer stock;
    private Integer remainStock;
    private Integer perUserLimit;
    private Integer status;
    private Integer version;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime preheatTime;
}
