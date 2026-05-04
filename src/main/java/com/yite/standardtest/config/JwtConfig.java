package com.yite.standardtest.config;


import com.yite.standardtest.common.interceptor.RepeatSubmitInterceptor;
import com.yite.standardtest.common.interceptor.TokenBucketRateLimitInterceptor;
import com.yite.standardtest.common.security.interceptor.JwtAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class JwtConfig implements WebMvcConfigurer {

    @Autowired
    private JwtAuthInterceptor jwtLoginInterceptor;

    @Autowired
    private RepeatSubmitInterceptor repeatSubmitInterceptor;

    @Autowired
    private TokenBucketRateLimitInterceptor tokenBucketRateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtLoginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/api/user/login",
                        "/**/*login*",
                        "/api/user/register",
                        "/api/user/refresh-token",
                        "/error",
                        "/upload/**" // 商品图片地址
                )
                .order(Ordered.HIGHEST_PRECEDENCE);

        registry.addInterceptor(repeatSubmitInterceptor)
                .addPathPatterns("/**")
                .order(Ordered.LOWEST_PRECEDENCE);

        registry.addInterceptor(tokenBucketRateLimitInterceptor)
                .addPathPatterns("/**")
                .order(Ordered.LOWEST_PRECEDENCE - 1);
    }
}
