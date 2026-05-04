package com.yite.standardtest.service.impl;

import com.yite.standardtest.service.RepeatTokenService;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RepeatTokenServiceImpl implements RepeatTokenService {

    /** 使用 StringRedisTemplate，保证 Redis 中为纯字符串，与 Lua 脚本 GET/比较一致 */
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String getRepeatToken() {
        String token = UUID.randomUUID().toString();
        String key = "repeat:token:" + token;
        stringRedisTemplate.opsForValue().set(key, token, 60, TimeUnit.SECONDS);
        return token;
    }

    @Override
    public Boolean verifyToken(String token) {
        String key = "repeat:token:" + token;
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }
}
