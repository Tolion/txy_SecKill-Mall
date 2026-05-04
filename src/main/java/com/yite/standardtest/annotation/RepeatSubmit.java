package com.yite.standardtest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RepeatSubmit {
    // 时间间隔（默认3秒）
    int interval() default 3;

    // 时间单位（默认秒）
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    // 业务唯一标识的SpEL表达式
    String key() default "";

    // 提示消息
    String message() default "操作过于频繁，请稍后再试";

    // 是否启用（可以动态开关）
    boolean enabled() default true;
}