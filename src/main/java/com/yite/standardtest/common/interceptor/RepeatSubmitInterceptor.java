package com.yite.standardtest.common.interceptor;

import com.yite.standardtest.annotation.RepeatSubmit;
import com.yite.standardtest.common.exception.RepeatSubmitException;
import com.yite.standardtest.common.security.context.LoginUser;
import com.yite.standardtest.common.security.context.LoginUserContext;
import com.yite.standardtest.common.util.SpELKeyResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 与 {@link RepeatSubmit} 配合：在单个 Lua 中原子完成「校验令牌 →（可选）业务时间窗锁 → 删除令牌」，
 * 避免「先加锁再验令牌」导致无效请求仍占锁的问题。
 */
@Component
public class RepeatSubmitInterceptor implements HandlerInterceptor {

    /** 与前端约定：提交防重复接口时在请求头携带一次性令牌 */
    public static final String HEADER_REPEAT_TOKEN = "X-Repeat-Token";

    /**
     * 返回：0=令牌无效；1=成功；2=时间窗内重复（SET NX 失败）
     */
    private static final String LUA_SUBMIT_GUARD =
            "local v = redis.call('GET', KEYS[1]); "
                    + "if v ~= ARGV[1] then return 0 end; "
                    + "local lk = ARGV[2]; "
                    + "if lk ~= '' then "
                    + "  local ttl = tonumber(ARGV[3]); "
                    + "  if ttl == nil or ttl <= 0 then ttl = 3 end; "
                    + "  local ok = redis.call('SET', lk, '1', 'NX', 'EX', ttl); "
                    + "  if not ok then return 2 end; "
                    + "end; "
                    + "redis.call('DEL', KEYS[1]); "
                    + "return 1";

    private final StringRedisTemplate stringRedisTemplate;
    private final SpELKeyResolver spELKeyResolver;

    @Autowired
    public RepeatSubmitInterceptor(StringRedisTemplate stringRedisTemplate, SpELKeyResolver spELKeyResolver) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.spELKeyResolver = spELKeyResolver;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 1.直接放行OPTIONS请求（CORS预检）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        // 2.若不是 Spring MVC 映射到某个 @RequestMapping 方法，就不做防重复，直接放行。
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        // 3.获取方法上的@RepeatSubmit注解
        RepeatSubmit repeatSubmit = handlerMethod.getMethodAnnotation(RepeatSubmit.class);
        if (repeatSubmit == null || !repeatSubmit.enabled()) {
            return true;
        }
        // 4.从请求头中获取防重复令牌
        String token = request.getHeader(HEADER_REPEAT_TOKEN);
        if (!StringUtils.hasText(token)) {
            throw new RepeatSubmitException("缺少防重复令牌，请先获取令牌后再提交");
        }
        // 5.构建业务锁的唯一标识
        String lockKey = "";
        String ttlArg = "0";
        // 6.若注解里配置了 key（SpEL）且 interval > 0，则构建业务锁的唯一标识
        if (StringUtils.hasText(repeatSubmit.key()) && repeatSubmit.interval() > 0) {
            // buildSpElExtra() 里通常会把当前登录用户的 userId 放进 Map，供 #userId 等表达式使用。
            Map<String, Object> extra = buildSpElExtra();
            // 7.解析SpEL表达式，spELKeyResolver.resolveKey(...) 把 SpEL 算成一个字符串 bizKey。
            String bizKey = spELKeyResolver.resolveKey(repeatSubmit.key(), request, extra);
            if (StringUtils.hasText(bizKey)) {
                // 8.若 bizKey 非空，则拼出 Redis 锁键：repeat:submit:lock:{bizKey}，并把过期秒数放进 ttlArg。
                lockKey = "repeat:submit:lock:" + bizKey;
                ttlArg = String.valueOf(repeatSubmit.timeUnit().toSeconds(repeatSubmit.interval()));
            }
        }

        // 9.调用Redis Lua脚本，原子性完成：
        //      1) 校验Token是否存在
        //      2) 存在则删除Token
        //      3) 返回操作结果（1=成功，0=失败）
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        // 10.Lua脚本 LUA_SUBMIT_GUARD 在上面。
        script.setScriptText(LUA_SUBMIT_GUARD);
        script.setResultType(Long.class);
        List<String> keys = Collections.singletonList("repeat:token:" + token);
        // 11.执行Lua脚本，返回操作结果（1=成功，0=失败）
        Long result = stringRedisTemplate.execute(script, keys, token, lockKey, ttlArg);
        if (result == null || result == 0L) {
            throw new RepeatSubmitException("重复提交或令牌已失效");
        }
        if (result == 2L) {
            throw new RepeatSubmitException(repeatSubmit.message());
        }
        return true;
    }

    // 从LoginUserContext里获取用户id
    private static Map<String, Object> buildSpElExtra() {
        Map<String, Object> extra = new HashMap<>();
        LoginUser user = LoginUserContext.get();
        if (user != null && user.getUserId() != null) {
            extra.put("userId", String.valueOf(user.getUserId()));
        }
        return extra;
    }
}
