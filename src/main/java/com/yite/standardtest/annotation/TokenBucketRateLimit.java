package com.yite.standardtest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 基于 Redis + Lua 的令牌桶限流注解（作用于 Controller 方法）。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TokenBucketRateLimit {

    /**
     * 限流桶的业务标识，用于区分不同接口。
     */
    String key();

    /**
     * 桶容量（最大令牌数）。
     */
    int capacity() default 10;

    /**
     * 每秒补充令牌数。
     */
    int refillTokensPerSecond() default 5;

    /**
     * 每次请求消耗令牌数。
     */
    int requestedTokens() default 1;

    /**
     * 限流提示文案。
     */
    String message() default "请求过于频繁，请稍后再试";
}
