package com.yite.standardtest.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yite.standardtest.DTO.ProductBuyDTO;
import com.yite.standardtest.DTO.ProductPayDTO;
import com.yite.standardtest.VO.ProductBuyVO;
import com.yite.standardtest.VO.ProductPayVO;
import com.yite.standardtest.VO.ProductVO;
import com.yite.standardtest.common.response.ResponseResult;
import com.yite.standardtest.entity.OrderEntity;
import com.yite.standardtest.entity.ProductEntity;
import com.yite.standardtest.service.PayService;
import com.yite.standardtest.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductsController {

    @Autowired
    private ProductService productService;

    private PayService payService;

    @GetMapping("/home")
    // 从 URL 里拿到 page 和 size
    public ResponseResult<Page<ProductVO>> productHomepage(@RequestParam(defaultValue = "1") Integer page,
                                                          @RequestParam(defaultValue = "5") Integer size) {

        // 调 productService.pageProductList(page, size) 去查数据库，拿到 Page<ProductEntity>。
        Page<ProductEntity> productPage = productService.pageProductList(page, size);

        // 将 product entity 转换为 product vo
        Page<ProductVO> voPage = (Page<ProductVO>) productPage.convert(product -> {
            ProductVO vo = new ProductVO();
            vo.setProductId(product.getId());
            vo.setName(product.getName());
            vo.setPrice(product.getPrice());
            vo.setImg(product.getImg());
            return vo;
        });

        return ResponseResult.success(voPage);
    }


    @PostMapping("/buy")
    public ResponseResult<ProductBuyVO> buyProduct(@RequestBody ProductBuyDTO dto) {
        try {
            // 调用 Service 下单
            ProductBuyVO vo = productService.buyProduct(dto);
            return ResponseResult.success(vo, "购买成功！");
        } catch (RuntimeException e) {
            // 捕获业务异常，返回统一格式
            return ResponseResult.error(e.getMessage());
        }
    }

}
