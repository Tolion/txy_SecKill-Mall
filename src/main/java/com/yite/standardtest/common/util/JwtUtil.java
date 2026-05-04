package com.yite.standardtest.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtUtil {

    private static final String SECRET_KEY =
            "PigLiXixieeeyJhbGciOiJIUzI1NiJ9ew0KICAic3ViIjogIjEyMzQ1Njc4OTAiLA0KICAibmFtZSI6ICJKb2huIERvZSIsDQogICJpYXQiOiAxNTE2MjM5MDIyDQp94AdcjHUFPA6Jv1c7ZIBqg";
    private static final long EXPIRE_TIME = 1000*60*10;     // 10 min
    private static final long EXPIRE_LONG_TIME = 1000*60*60*24*30;     // 30 days

    public static String generateToken(Long id, String username){
        return Jwts.builder()
                .claim("userId", id)
                .claim("username", username)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public static String generateLongToken(Long id, String username){
        return Jwts.builder()
                .claim("userId", id)
                .claim("username", username)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_LONG_TIME))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public static Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
    }
}
