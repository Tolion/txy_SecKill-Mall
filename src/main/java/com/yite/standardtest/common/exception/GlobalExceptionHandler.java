package com.yite.standardtest.common.exception;

import com.yite.standardtest.common.response.ResponseResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 拦截器校验失败时抛出 {@link RepeatSubmitException}，HTTP 429 与业务码一致。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RepeatSubmitException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ResponseResult<Void> handleRepeatSubmitException(RepeatSubmitException e) {
        return ResponseResult.error(429, e.getMessage());
    }

    @ExceptionHandler(RateLimitException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ResponseResult<Void> handleRateLimitException(RateLimitException e) {
        return ResponseResult.error(430, e.getMessage());
    }
}
