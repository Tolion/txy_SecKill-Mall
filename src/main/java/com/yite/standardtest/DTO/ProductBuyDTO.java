package com.yite.standardtest.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductBuyDTO {
    private Long productId;
    private Integer quantity;
}
