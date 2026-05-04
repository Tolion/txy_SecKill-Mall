package com.yite.standardtest.service;

import com.yite.standardtest.DTO.ProductPayDTO;
import com.yite.standardtest.VO.ProductPayVO;

public interface PayService {
    /**
     * 支付商品
     */
    ProductPayVO payProduct(ProductPayDTO dto);
}
