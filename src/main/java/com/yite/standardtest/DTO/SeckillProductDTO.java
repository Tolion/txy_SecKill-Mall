package com.yite.standardtest.DTO;

import lombok.Data;

@Data
public class SeckillProductDTO {
    /**
     * 秒杀商品 ID（对应 t_seckill_product.id）
     */
    private Long seckillProductId;

    /**
     * 购买数量，支持按秒杀商品限购数量发起购买
     */
    private Integer quantity;
}
