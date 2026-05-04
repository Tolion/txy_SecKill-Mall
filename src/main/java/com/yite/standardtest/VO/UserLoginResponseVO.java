package com.yite.standardtest.VO;

import lombok.Data;

@Data
public class UserLoginResponseVO {
    private Long id;
    public String username;
    private String jwt_token;
    private String long_jwt_token;
}
