package com.yite.standardtest.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class SeckillMessageDTO {
    private String messageId;
    private String secOrderNo;
    private Long userId;
    private Long seckillActivityId;
    private Long seckillProductId;
    private Integer version;
    private Integer quantity;
    private BigDecimal seckillUnitPrice;
    private BigDecimal originUnitPrice;
    private BigDecimal discountAmount;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime cancelTime;
    private LocalDateTime expireTime;
}
