package com.yite.standardtest.controller;

import com.yite.standardtest.annotation.RepeatSubmit;
import com.yite.standardtest.common.response.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/coupon")
public class CouponController {

    /** 领取优惠券（演示防重复）：需登录，请求头携带 X-Repeat-Token；业务键为当前用户 id（SpEL #userId）。 */
    @PostMapping("/claim")
    @RepeatSubmit(
            key = "#userId",
            interval = 3,
            message = "操作过于频繁，请稍后再试")
    public ResponseResult<String> claimCoupon() {
        return ResponseResult.success("5元新人券已发放至账户（演示）", "领取成功");
    }
}
