package com.yite.standardtest.common.security.context;

// 本质是封装 ThreadLocal ，使其实例不暴露给外部，仅提供部分的访问方法。
public final class LoginUserContext {

    // 给“当前线程”绑定一份独立的数据
    private static final ThreadLocal<LoginUser> CONTEXT = new ThreadLocal<>();

    private LoginUserContext() {}

    public static void set(LoginUser user) {
        CONTEXT.set(user);
    }

    public static LoginUser get() {
        return CONTEXT.get();
    }

    public static Long getUserId() {
        LoginUser user = CONTEXT.get();
        return user == null ? null : user.getUserId();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
