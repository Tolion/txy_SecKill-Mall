package com.yite.standardtest.common.interceptor;

import com.yite.standardtest.annotation.TokenBucketRateLimit;
import com.yite.standardtest.common.exception.RateLimitException;
import com.yite.standardtest.common.security.context.LoginUserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import lombok.extern.slf4j.Slf4j;

/**
 * 令牌桶限流拦截器。
 *
 * 工作流程：
 * 1. 拦截所有进入 Spring MVC 的请求
 * 2. 只对标注了 {@link TokenBucketRateLimit} 的接口生效
 * 3. 根据业务 key + 请求方法 + 用户/ IP 维度生成唯一桶 key
 * 4. 使用 Redis Lua 脚本原子完成“补充令牌 + 扣减令牌”
 * 5. 令牌不足时抛出 {@link RateLimitException}
 *
 * 适用场景：
 * - 秒杀接口限流
 * - 高并发提交表单限流
 * - 按用户或 IP 粒度做访问控制
 */
@Slf4j
@Component
public class TokenBucketRateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TokenBucketRateLimitInterceptor.class);

    /**
     * Lua 脚本：令牌桶核心逻辑。
     *
     * 参数说明：
     * - KEYS[1]：令牌桶 Redis Key
     * - ARGV[1]：当前时间戳（毫秒）
     * - ARGV[2]：桶容量
     * - ARGV[3]：每秒补充令牌数
     * - ARGV[4]：本次请求所需令牌数
     * - ARGV[5]：Key 过期时间（秒）
     *
     * 返回值说明：
     * - 1：放行，扣减成功
     * - 0：限流，令牌不足
     */
    private static final String LUA_TOKEN_BUCKET =
            "local tokenKey = KEYS[1]; "
                    + "local now = tonumber(ARGV[1]); "
                    + "local capacity = tonumber(ARGV[2]); "
                    + "local refillRate = tonumber(ARGV[3]); "
                    + "local requested = tonumber(ARGV[4]); "
                    + "local ttl = tonumber(ARGV[5]); "
                    + "local tokens = tonumber(redis.call('HGET', tokenKey, 'tokens')); "
                    + "local ts = tonumber(redis.call('HGET', tokenKey, 'ts')); "
                    + "if tokens == nil then tokens = capacity end; "
                    + "if ts == nil then ts = now end; "
                    + "local deltaMs = now - ts; "
                    + "if deltaMs < 0 then deltaMs = 0 end; "
                    + "local refill = (deltaMs * refillRate) / 1000.0; "
                    + "tokens = math.min(capacity, tokens + refill); "
                    + "if tokens < requested then "
                    + "  redis.call('HSET', tokenKey, 'tokens', tokens); "
                    + "  redis.call('HSET', tokenKey, 'ts', now); "
                    + "  redis.call('EXPIRE', tokenKey, ttl); "
                    + "  return 0; "
                    + "end; "
                    + "tokens = tokens - requested; "
                    + "redis.call('HSET', tokenKey, 'tokens', tokens); "
                    + "redis.call('HSET', tokenKey, 'ts', now); "
                    + "redis.call('EXPIRE', tokenKey, ttl); "
                    + "return 1;";

    /**
     * Redis 操作模板，用于执行 Lua 脚本。
     */
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 构造注入 RedisTemplate。
     */
    @Autowired
    public TokenBucketRateLimitInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 请求预处理：在进入 Controller 之前做令牌桶校验。
     *
     * @param request 当前请求
     * @param response 当前响应
     * @param handler 当前处理器，可能是 Controller 方法，也可能是静态资源处理器
     * @return true 放行；false 拦截（本实现直接通过异常处理拦截）
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. OPTIONS 预检请求直接放行，避免跨域请求被限流误伤。
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 2. 只有 Controller 方法才需要检查注解。
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 3. 读取方法上的 TokenBucketRateLimit 注解。
        TokenBucketRateLimit limit = handlerMethod.getMethodAnnotation(TokenBucketRateLimit.class);
        if (limit == null) {
            return true;
        }

        // 4. 校验注解参数的合法性。
        validate(limit);

        // 5. 根据业务 key + 请求信息构造桶 key。
        String bucketKey = buildBucketKey(limit.key(), request);

        // 6. 构造 Lua 脚本对象。
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(LUA_TOKEN_BUCKET);
        script.setResultType(Long.class);

        // 7. Lua 脚本只需要一个 Redis key。
        List<String> keys = Collections.singletonList(bucketKey);

        long now = System.currentTimeMillis();

        // 8. 设置桶 key 的过期时间，避免长期无访问时 Redis 残留无用数据。
        long ttlSeconds = Math.max(2L, (long) Math.ceil((double) limit.capacity() / limit.refillTokensPerSecond()) * 2L);

        // 打印关键参数，便于排查 Lua 脚本执行问题。
        log.info("TokenBucketRateLimit params: bucketKey={}, now={}, capacity={}, refillTokensPerSecond={}, requestedTokens={}, ttlSeconds={}",
                bucketKey, now, limit.capacity(), limit.refillTokensPerSecond(), limit.requestedTokens(), ttlSeconds);

        log.info("nownownow: {}", now);     //1777650456861
        log.info("ttlSecondsttlSeconds: {}", ttlSeconds);   //6
        
        // 9. 执行 Lua：原子地完成补充令牌与扣减令牌。
        Long result = stringRedisTemplate.execute(
                script,
                keys,
                String.valueOf(now),
                String.valueOf(limit.capacity()),
                String.valueOf(limit.refillTokensPerSecond()),
                String.valueOf(limit.requestedTokens()),
                String.valueOf(ttlSeconds)
        );

        // 10. Lua 返回 0，说明当前令牌不足，直接抛出限流异常。
        if (result == null || result == 0L) {
            throw new RateLimitException(limit.message());
        }
        return true;
    }

    /**
     * 校验注解中的参数，避免配置错误导致限流逻辑异常。
     */
    private static void validate(TokenBucketRateLimit limit) {
        if (limit.capacity() <= 0 || limit.refillTokensPerSecond() <= 0 || limit.requestedTokens() <= 0) {
            throw new IllegalArgumentException("TokenBucketRateLimit 参数必须大于 0");
        }
        if (!StringUtils.hasText(limit.key())) {
            throw new IllegalArgumentException("TokenBucketRateLimit key 不能为空");
        }
    }

    /**
     * 构造令牌桶的 Redis key。
     *
     * 规则：
     * rate:tb:{业务key}:{请求方法}:{用户维度}
     *
     * 用户维度优先使用 userId；如果用户未登录，则降级为 IP。
     */
    private static String buildBucketKey(String bizKey, HttpServletRequest request) {
        Long userId = LoginUserContext.getUserId();
        String dim;
        if (userId != null) {
            dim = "uid:" + userId;
        } else {
            dim = "ip:" + clientIp(request);
        }
        return "rate:tb:" + bizKey + ":" + request.getMethod() + ":" + dim;
    }

    /**
     * 尽量兼容代理环境，按优先级获取客户端真实 IP：
     * 1. X-Forwarded-For
     * 2. X-Real-IP
     * 3. request.getRemoteAddr()
     */
    private static String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xff)) {
            String[] arr = xff.split(",");
            if (arr.length > 0 && StringUtils.hasText(arr[0])) {
                return arr[0].trim();
            }
        }
        String rip = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(rip)) {
            return rip.trim();
        }
        return request.getRemoteAddr();
    }
}
