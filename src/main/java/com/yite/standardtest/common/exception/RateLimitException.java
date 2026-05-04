package com.yite.standardtest.common.exception;

/**
 * 令牌桶限流触发时抛出。
 */
public class RateLimitException extends RuntimeException {

    public RateLimitException(String message) {
        super(message);
    }
}
