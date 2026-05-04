package com.yite.standardtest.controller;

import com.yite.standardtest.common.response.ResponseResult;
import com.yite.standardtest.service.RepeatTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/repeat-token")
public class RepeatTokenController {

    @Autowired
    private RepeatTokenService repeatTokenService;

    /** 进入页面或点击前调用，获取一次性令牌（60s 内有效） */
    @GetMapping("/get-token")
    public ResponseResult<String> getRepeatToken() {
        String token = repeatTokenService.getRepeatToken();
        return ResponseResult.success(token);
    }

    @PostMapping("/verify-token")
    public ResponseResult<Boolean> verifyToken(@RequestParam String token) {
        Boolean result = repeatTokenService.verifyToken(token);
        return ResponseResult.success(result);
    }
}
