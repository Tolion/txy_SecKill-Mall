package com.yite.standardtest.service;

import com.yite.standardtest.DTO.UserLoginRequestDTO;
import com.yite.standardtest.VO.UserLoginResponseVO;
import com.yite.standardtest.DTO.UserRegisterDTO;


public interface UserService {

    /**
     * 用户注册
     */
    public void register(UserRegisterDTO user);

    /**
     * 用户登录
     */
    UserLoginResponseVO login(UserLoginRequestDTO user);

    /**
     * 使用 Refresh Token 刷新 Access Token
     */
    UserLoginResponseVO refreshToken(String refreshToken);

}



