package com.yb.forum.utils;

import com.yb.forum.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;

public class JwtUtil {
    // 使用足够长的密钥（64 字节 = 512 位，满足 HS512 要求）



    private static final String SECRET_KEY = "forum-graduation-project-secret-key-2026-yangbiao-secure-jwt-token-key";
    private static final long EXPIRATION_TIME = 86400000; // 1 天 (24 小时)
    private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    /**
     * 生成 JWT 令牌
     * @param user 用户对象
     * @return JWT 令牌字符串
     */
    public static String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .claim("nickname", user.getNickname())
                .claim("avatarUrl", user.getAvatarUrl())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SIGNING_KEY, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 解析 JWT 令牌
     * @param token JWT 令牌字符串
     * @return Claims 对象，包含用户信息
     */
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SIGNING_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从令牌中获取用户 ID
     * @param token JWT 令牌
     * @return 用户 ID
     */
    public static Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 验证令牌是否有效
     * @param token JWT 令牌
     * @return true 表示有效
     */
    public static boolean isTokenValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查令牌是否过期
     * @param token JWT 令牌
     * @return true 表示已过期
     */
    public static boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
