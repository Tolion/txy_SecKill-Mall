package com.yite.standardtest.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yite.standardtest.DTO.ProductBuyDTO;
import com.yite.standardtest.VO.ProductBuyVO;
import com.yite.standardtest.entity.ProductEntity;
import com.yite.standardtest.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductServiceImplTest {

    @Autowired
    private ProductService productService;

    @Test
    void pageProductList() {
        // given
        int page = 1;
        int size = 10;

        // when
        Page<ProductEntity> result = productService.pageProductList(page, size);

        // then
        assertNotNull(result, "分页结果不能为 null");
        assertEquals(page, result.getCurrent(), "当前页码不正确");
        assertEquals(size, result.getSize(), "分页大小不正确");
        assertNotNull(result.getRecords(), "记录列表不能为 null");

    }

    @Test
    void buyProduct() {
        // give
        ProductBuyDTO dto = new ProductBuyDTO(7L,4);

        ProductBuyVO vo = productService.buyProduct(dto);

        // 验证返回 VO
        assertNotNull(vo, "返回结果不能为 null");
        assertEquals(4, vo.getQuantity(), "商品数量不正确");
        assertEquals(BigDecimal.valueOf(15.80), vo.getSinglePrice(), "商品单价不正确");
    }
}