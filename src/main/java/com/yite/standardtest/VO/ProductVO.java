package com.yite.standardtest.VO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVO {
    private Long productId;
    private String name;
    private BigDecimal price;
    private String img;
}
