package com.yite.standardtest.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_seckill_order")
public class SeckillOrderEntity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
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
    private LocalDateTime updateTime;
}