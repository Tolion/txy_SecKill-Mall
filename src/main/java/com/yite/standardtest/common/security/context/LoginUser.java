package com.yite.standardtest.common.security.context;

import lombok.Data;

@Data
public class LoginUser {

    private Long userId;
    private String username;

    public LoginUser(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }
    // 后续可以加 role、permissions


}
