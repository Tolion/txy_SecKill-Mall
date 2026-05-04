package com.yite.standardtest.common.util;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 解析 {@link com.yite.standardtest.annotation.RepeatSubmit} 中 {@code key} 的 SpEL 表达式，生成业务锁唯一标识。
 *
 * <p>支持的表达式示例：
 * <ul>
 *   <li>{@code #userId}：取同名请求参数（首个值）</li>
 *   <li>{@code #order.id}：取 {@code additionalVariables} 中名为 {@code order} 的对象的嵌套属性（需调用方传入请求体等）</li>
 *   <li>{@code #request.getHeader('deviceId')}：取请求头（SpEL 字符串请用单引号）</li>
 * </ul>
 */
@Component
public class SpELKeyResolver {

    private final SpelExpressionParser parser = new SpelExpressionParser();

    /**
     * 从当前线程的 {@link RequestContextHolder} 取请求并解析；无请求上下文时返回空串。
     */
    public String resolveKey(String keyExpression) {
        if (!StringUtils.hasText(keyExpression)) {
            return "";
        }
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return "";
        }
        return resolveKey(keyExpression, attrs.getRequest(), null);
    }

    /**
     * 仅注入 {@link HttpServletRequest}：参数名作为变量，{@code request} 变量指向当前请求。
     */
    public String resolveKey(String keyExpression, HttpServletRequest request) {
        return resolveKey(keyExpression, request, null);
    }

    /**
     * 解析 SpEL：将请求参数、{@code request}，以及可选的额外变量（如方法参数名、{@code @RequestBody} 绑定名）放入上下文后求值。
     *
     * @param keyExpression SpEL 表达式，与 {@link com.yite.standardtest.annotation.RepeatSubmit#key()} 一致
     * @param request 当前请求
     * @param additionalVariables 可选；如 {@code order} → 反序列化后的订单对象，用于 {@code #order.id}
     */
    public String resolveKey(
            String keyExpression, HttpServletRequest request, Map<String, Object> additionalVariables) {
        if (!StringUtils.hasText(keyExpression)) {
            return "";
        }
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("request", request);
        for (Map.Entry<String, String[]> e : request.getParameterMap().entrySet()) {
            String[] values = e.getValue();
            if (values != null && values.length > 0) {
                context.setVariable(e.getKey(), values[0]);
            }
        }
        if (additionalVariables != null) {
            for (Map.Entry<String, Object> e : additionalVariables.entrySet()) {
                if (e.getKey() != null) {
                    context.setVariable(e.getKey(), e.getValue());
                }
            }
        }
        Object value = parser.parseExpression(keyExpression).getValue(context);
        return value != null ? value.toString() : "";
    }
}
