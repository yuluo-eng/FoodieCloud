package com.example.springbootblank.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class JwtServiceTest {

    private long time = 1000 * 60 * 60 * 24;
    // Base64 编码后的密钥（解码后需 >= 32 字节，满足 HS256 最低 256-bit 要求）
    private String secret = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    public String buildJwtToken() {
        // 生成阶段：先把 Base64 密钥解码为真实字节，再构造 HS256 可用的签名密钥。
        SecretKey signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        JwtBuilder builder = Jwts.builder();
        return builder
                // JWT 原理：
                // 1) Header：声明令牌类型与签名算法（如 HS256）；
                // 2) Payload：存放业务声明（claims），例如用户名、角色、过期时间；
                // 3) Signature：使用密钥对“Header.Payload”签名，服务端据此校验是否被篡改。
                //    客户端可解码前两段查看内容，但无法伪造合法签名。
                //    JWT 默认不加密（只是 Base64Url 编码），敏感信息不应直接放入 payload。
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                .claim("username", "admin")
                .claim("role", "admin")
                .setSubject("admin-test")
                .setExpiration(new Date(System.currentTimeMillis() + time))
                .setId(UUID.randomUUID().toString())
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    public void jwtServiceTest() {
        String jwtToken = buildJwtToken();
        System.out.println(jwtToken);
    }

    @Test
    public void parseTest() {
        // 解析流程概览：传入 jwtToken → 用与签发相同的密钥验签（parseSignedClaims 内部完成）→ 验签通过后再取 payload → 读业务字段。
        String jwtToken = buildJwtToken();
        // 解析阶段第 1 步：准备同一把签名密钥（验签必须与签发密钥一致）。
        SecretKey signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        // 解析阶段第 2 步：verifyWith 指定验签密钥；parseSignedClaims 会校验签名、结构、过期等，失败则抛异常，成功才返回。
        Jws<Claims> claimsJws = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(jwtToken);
        // 解析阶段第 3 步：从 payload 里读取明文 claims（业务字段）。
        Claims claims = claimsJws.getPayload();

        // assertNotNull：JUnit 断言「claims 不能为 null」；若为 null 则测试失败。用于确认解析后确实拿到了 payload。
        assertNotNull(claims);
        // assertEquals(期望值, 实际值)：断言「实际值必须等于期望值」，否则测试失败。用于自动校验解析结果与签发时写入的内容一致。
        assertEquals("admin", claims.get("username", String.class));
        assertEquals("admin", claims.get("role", String.class));
        assertEquals("admin-test", claims.getSubject());

        System.out.println("username: " + claims.get("username"));
        System.out.println("role: " + claims.get("role"));
        System.out.println("subject: " + claims.getSubject());
        System.out.println("id: " + claims.getId());
        System.out.println("expiration: " + claims.getExpiration());
        
    }

}