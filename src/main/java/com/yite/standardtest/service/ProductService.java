package com.yite.standardtest.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yite.standardtest.DTO.ProductBuyDTO;
import com.yite.standardtest.DTO.ProductPayDTO;
import com.yite.standardtest.VO.ProductBuyVO;
import com.yite.standardtest.VO.ProductPayVO;
import com.yite.standardtest.entity.ProductEntity;

public interface ProductService {

    /**
     * 获取首页商品列表
     */
    Page<ProductEntity> pageProductList(int page, int size);

    /**
     * 购买商品
     */
    ProductBuyVO buyProduct(ProductBuyDTO dto);

}
