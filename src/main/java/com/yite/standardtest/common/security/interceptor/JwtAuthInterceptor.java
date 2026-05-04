package com.yite.standardtest.common.security.interceptor;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.yite.standardtest.common.security.context.LoginUser;
import com.yite.standardtest.common.security.context.LoginUserContext;
import com.yite.standardtest.common.util.JwtUtil;
import com.yite.standardtest.controller.ProductsController;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // 0. 直接放行 OPTIONS 请求（CORS 预检）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 1.从 Header 取 token
        String authHeader = request.getHeader("Authorization");

        if (StringUtils.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "未登录或 token 缺失");
            return false;
        }

        // 2. 去掉 Bearer 前缀
        String token = authHeader.substring(7);

        try {
            // 3. 校验 token（是否过期、签名是否合法）
            Claims claims = JwtUtil.parseToken(token);

            // 4. 解析用户信息
            Long userId = claims.get("userId", Long.class);
            //String username = claims.getSubject();    // 主键
            String username = claims.get("username", String.class);

            // 5. 放入 ThreadLocal，供 Service 层使用
            LoginUserContext.set(new LoginUser(userId, username));
            // 以前原始的方法是，设置变量
            //request.setAttribute("userId", claims.get("userId"));

            // 后续 Controller / Service / Mapper 之前任意层使用：
            LoginUser user = LoginUserContext.get();
            Long id = user.getUserId();
            String username1 = user.getUsername();

            log.info("TXY - LoginUser getUserId: " + id);
            log.info("TXY - LoginUser getUsername: " + username1);

            return true;

        } catch (ExpiredJwtException e) {   // 返回 401，并写出：{"code":401,"message":"token 已过期"}
            writeUnauthorized(response, "token 已过期");
            return false;
        } catch (Exception e) {
            writeUnauthorized(response, "token 非法");
            return false;
        }

    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        // 6. 请求结束后清理 ThreadLocal（非常重要）
        LoginUserContext.clear();
    }

    // 自定义未授权响应的方法，用于向前端返回统一的未授权信息格式。
    private void writeUnauthorized(HttpServletResponse response, String msg)
            throws IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"code\":401,\"message\":\"" + msg + "\"}"
        );
    }

}
