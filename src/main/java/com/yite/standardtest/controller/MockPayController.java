package com.yite.standardtest.controller;

import com.yite.standardtest.DTO.MockPayDTO;
import com.yite.standardtest.DTO.ProductPayDTO;
import com.yite.standardtest.VO.ProductBuyVO;
import com.yite.standardtest.VO.ProductPayVO;
import com.yite.standardtest.common.response.ResponseResult;
import com.yite.standardtest.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mock")
public class MockPayController {

    @Autowired
    private PayService payService;

    // 模拟创建支付单
    @PostMapping("/pay")
    public ResponseResult<ProductPayVO> payProduct(@RequestBody ProductPayDTO dto) {
        ProductPayVO vo = payService.payProduct(dto);
        return ResponseResult.success(vo, "支付成功！");
    }

    // （可选）模拟回调接口 /notify，可用于未来接真实第三方支付
    // 只需替换 MockController → 调用支付宝 / 微信 SDK
    @PostMapping("/notify")
    public String notify(@RequestParam String orderNo) {
        // 这里直接调用支付Service即可
        ProductPayDTO dto = new ProductPayDTO();
        dto.setOrderNo(orderNo);
        return "SUCCESS";
    }
}
