package com.yite.standardtest.controller;

import com.yite.standardtest.DTO.UserLoginRequestDTO;
import com.yite.standardtest.DTO.RefreshTokenRequestDTO;
import com.yite.standardtest.VO.UserLoginResponseVO;
import com.yite.standardtest.DTO.UserRegisterDTO;
import com.yite.standardtest.annotation.TokenBucketRateLimit;
import com.yite.standardtest.common.response.ResponseResult;
import com.yite.standardtest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseResult<Void> register(@RequestBody UserRegisterDTO dto) {
        userService.register(dto);
        return ResponseResult.successMessage("注册成功");
    }

    @PostMapping("/login")
    @TokenBucketRateLimit(
            key = "user_login",
            capacity = 6,
            refillTokensPerSecond = 2,
            requestedTokens = 1,
            message = "登录请求过于频繁，请稍后重试"
    )
    public ResponseResult<UserLoginResponseVO> login(@RequestBody UserLoginRequestDTO dto) {
        UserLoginResponseVO data = userService.login(dto);
        return ResponseResult.success(data);
    }

    @PostMapping("/refresh-token")
    public ResponseResult<UserLoginResponseVO> refreshToken(@RequestBody RefreshTokenRequestDTO dto) {
        try {
            UserLoginResponseVO data = userService.refreshToken(dto.getRefreshToken());
            return ResponseResult.success(data);
        } catch (RuntimeException e) {
            // 刷新失败统一返回 401，前端据此触发重新登录
            return ResponseResult.error(401, e.getMessage());
        }
    }
}


