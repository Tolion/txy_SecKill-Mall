package com.yite.standardtest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.yite.standardtest.DTO.UserLoginRequestDTO;
import com.yite.standardtest.VO.UserLoginResponseVO;
import com.yite.standardtest.DTO.UserRegisterDTO;
import com.yite.standardtest.common.util.JwtUtil;
import com.yite.standardtest.entity.UserEntity;
import com.yite.standardtest.mapper.UserMapper;
import com.yite.standardtest.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired  // 注入 RedisTemplate，用于管理 Refresh Token
    private RedisTemplate<String, Object> redisTemplate;

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh_token:";

    @Override
    public void register(UserRegisterDTO dto) {

        // 1. 参数校验
        if (StringUtils.isBlank(dto.getUsername()) ||
                StringUtils.isBlank(dto.getPassword())) {
            throw new RuntimeException("用户名或密码不能为空");
        }

        // 2. 用户名是否已存在
        LambdaQueryWrapper<UserEntity> query = new LambdaQueryWrapper<>();
        query.eq(UserEntity::getUsername, dto.getUsername());
        if (userMapper.selectCount(query) > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 3. 密码加密
        String encryptedPassword = passwordEncoder.encode(dto.getPassword());

        // 4. 保存用户
        UserEntity user = new UserEntity();
        user.setUsername(dto.getUsername());
        user.setPassword(encryptedPassword);
        user.setPhone(dto.getPhone());
        user.setStatus(1);

        userMapper.insert(user);
    }

    @Override
    public UserLoginResponseVO login(UserLoginRequestDTO dto){

        // 1. 参数校验，是否为空
        if (StringUtils.isBlank(dto.getUsername()) ||
                StringUtils.isBlank(dto.getPassword())) {
            throw new RuntimeException("用户名或密码不能为空");
        }

        // 2. 根据用户名查询用户
        LambdaQueryWrapper<UserEntity> query = new LambdaQueryWrapper<>();
        query.eq(UserEntity::getUsername, dto.getUsername());
        UserEntity user = userMapper.selectOne(query);

        // 3. 用户是否存在
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 4. 用户状态校验
        if (user.getStatus() != 1) {
            throw new RuntimeException("用户已被禁用");
        }

        // 5. 校验密码（加密）
        boolean isPasswordMatch = passwordEncoder.matches(dto.getPassword(), user.getPassword());
        if (!isPasswordMatch) {
            throw new RuntimeException("用户名或密码错误");
        }

        log.info("TXY - dto.getPassword()的值: " + dto.getPassword());
        log.info("TXY - encryptedPassword的值/密码编码后的值: " + user.getPassword());

        // 6.生成 Access Token（短期）
        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        log.info("TXY - user.getId()的值: " + user.getId());
        log.info("TXY - user.getUsername()的值 " + user.getUsername());
        log.info("TXY - generateToken: " + token);

        // 生成 Refresh Token（长期）
        String longToken = JwtUtil.generateLongToken(user.getId(), user.getUsername());
        // 将 Refresh Token 写入 Redis：key=refresh_token:xxx, value=userId, TTL=30天
        String refreshKey = REFRESH_TOKEN_KEY_PREFIX + longToken;
        redisTemplate.opsForValue().set(refreshKey, user.getId().toString(), 30, TimeUnit.DAYS);
        log.info("TXY - generateLLLLLongToken: " + longToken);

        // 7. 组装返回 DTO（Entity → DTO）
        UserLoginResponseVO responseDTO = new UserLoginResponseVO();
        responseDTO.setId(user.getId());
        responseDTO.setUsername(user.getUsername());
        responseDTO.setJwt_token(token);
        responseDTO.setLong_jwt_token(longToken);

        return responseDTO;
    }

    @Override
    public UserLoginResponseVO refreshToken(String refreshToken) {
        if (StringUtils.isBlank(refreshToken)) {
            throw new RuntimeException("refresh token 不能为空");
        }

        String redisKey = REFRESH_TOKEN_KEY_PREFIX + refreshToken;
        Object userIdObj = redisTemplate.opsForValue().get(redisKey);
        if (userIdObj == null) {
            throw new RuntimeException("refresh token 已失效，请重新登录");
        }

        Claims claims;
        try {
            claims = JwtUtil.parseToken(refreshToken);
        } catch (ExpiredJwtException e) {
            // 过期的 refresh token 直接删除
            redisTemplate.delete(redisKey);
            throw new RuntimeException("refresh token 已过期，请重新登录");
        } catch (Exception e) {
            throw new RuntimeException("refresh token 非法");
        }

        Long userIdFromToken = claims.get("userId", Long.class);
        String username = claims.get("username", String.class);
        Long userIdFromRedis = Long.valueOf(userIdObj.toString());

        if (!userIdFromRedis.equals(userIdFromToken)) {
            throw new RuntimeException("refresh token 与用户不匹配");
        }

        // 生成新的 Access Token 和 Refresh Token
        String newAccessToken = JwtUtil.generateToken(userIdFromToken, username);
        String newRefreshToken = JwtUtil.generateLongToken(userIdFromToken, username);

        // 更新 Redis：删除旧的，写入新的
        redisTemplate.delete(redisKey);
        String newRedisKey = REFRESH_TOKEN_KEY_PREFIX + newRefreshToken;
        redisTemplate.opsForValue().set(newRedisKey, userIdFromToken.toString(), 30, TimeUnit.DAYS);
        log.info("过期重新：GGGGenerateLLLLLongToken: " + newRefreshToken);

        // 组装返回 DTO
        UserLoginResponseVO responseDTO = new UserLoginResponseVO();
        responseDTO.setId(userIdFromToken);
        responseDTO.setUsername(username);
        responseDTO.setJwt_token(newAccessToken);
        responseDTO.setLong_jwt_token(newRefreshToken);

        return responseDTO;
    }
}
