package com.yite.standardtest.service;

public interface RepeatTokenService {
    /**
     * 获取重复提交的令牌
     */
    String getRepeatToken();

    /**
     * 验证重复提交的令牌
     */
    Boolean verifyToken(String token);
}