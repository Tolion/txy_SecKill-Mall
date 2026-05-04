package com.yite.standardtest.DTO;

import lombok.Data;

@Data
public class RefreshTokenRequestDTO {

    /**
     * 刷新用的长效 JWT（Refresh Token）
     */
    private String refreshToken;
}

