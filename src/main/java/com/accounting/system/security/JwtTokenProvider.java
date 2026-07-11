package com.accounting.system.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

/**
 * ============================================================
 * JWT 令牌提供器 —— Token的生成与解析中枢
 * ============================================================
 *
 * 【什么是JWT】
 * JSON Web Token，一种紧凑的、URL安全的令牌格式，由三部分组成：
 *   Header.Payload.Signature
 *   - Header： 算法类型（HS256）
 *   - Payload： 用户信息（userId, username, role）+ 过期时间
 *   - Signature：对前两部分的签名，防止篡改
 *
 * 【本项目JWT设计】
 * Token中包含三个自定义声明(Claim)：
 *   - sub (subject)：userId —— 用于标识"谁"拥有此Token
 *   - username：用户名 —— 辅助信息
 *   - role：角色（USER/ADMIN）—— 用于权限判断
 *
 * 【签名机制】
 * 使用 HMAC-SHA256 对称签名：
 *   - 密钥来源：application.yml 中的 jwt.secret（Base64编码）
 *   - 签发时：用密钥对payload签名
 *   - 验证时：重新计算签名 → 比对，不一致则说明被篡改
 *   - 这意味着：密钥必须保密！任何人持有密钥都能签发合法Token
 *
 * 【Token生命周期】
 * 签发(login) → 每次请求携带 → 到期自动失效(24h)
 * 无状态设计：服务器不记录谁持有Token，只验证签名和过期时间
 */
@Component
public class JwtTokenProvider {

    /** HMAC签名密钥，由配置文件中的Base64字符串解码而来 */
    private final SecretKey key;
    /** Token有效期（毫秒），默认24小时 */
    private final long expiration;

    /**
     * 构造函数注入配置值
     * @param secret    Base64编码的密钥字符串（来自 application.yml 的 jwt.secret）
     * @param expiration Token有效期（来自 application.yml 的 jwt.expiration）
     */
    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.expiration}") long expiration) {
        // HS256算法要求密钥至少256位，Base64解码后生成SecretKey对象
        this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
        this.expiration = expiration;
    }

    /**
     * 生成JWT Token（登录成功时调用）
     *
     * @param userId   用户ID，存入sub字段
     * @param username 用户名，存入自定义claim
     * @param role     角色（USER/ADMIN），存入自定义claim
     * @return 签发的JWT字符串，格式：xxx.yyy.zzz
     */
    public String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userId.toString())   // sub：令牌主体（用户ID）
                .claim("username", username)  // 自定义声明：用户名
                .claim("role", role)          // 自定义声明：角色
                .issuedAt(now)                // iat：签发时间
                .expiration(expiryDate)        // exp：过期时间
                .signWith(key)                // 使用HMAC-SHA256签名
                .compact();                   // 序列化为字符串
    }

    /**
     * 从Token中提取用户ID
     * @param token JWT字符串
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从Token中提取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 从Token中提取角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    /**
     * 验证Token是否有效
     * 检查项：签名正确 + 未过期 + 格式合法
     *
     * @param token JWT字符串
     * @return true=有效, false=无效（过期/签名错误/格式不对）
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // JwtException：签名无效、已过期、格式错误等
            // IllegalArgumentException：Token为空等
            return false;
        }
    }

    /**
     * 解析Token的核心方法
     * 
     * 执行步骤：
     * 1. 用密钥验证签名（防篡改）
     * 2. 检查过期时间（exp声明）
     * 3. 提取Payload中的Claims
     *
     * @param token JWT字符串
     * @return Token中的声明信息
     * @throws JwtException 签名无效或Token过期时抛出
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)        // 设置验证密钥
                .build()                // 构建解析器
                .parseSignedClaims(token) // 解析并验证签名
                .getPayload();          // 获取Payload（Claims）
    }
}
