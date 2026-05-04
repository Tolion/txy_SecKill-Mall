package com.yite.standardtest.common.exception;

/**
 * 防重复提交校验失败（令牌无效、时间窗口内重复等）。
 */
public class RepeatSubmitException extends RuntimeException {

    public RepeatSubmitException(String message) {
        super(message);
    }
}
