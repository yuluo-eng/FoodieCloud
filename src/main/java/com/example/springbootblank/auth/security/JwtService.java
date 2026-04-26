package com.example.springbootblank.auth.security;

import com.example.springbootblank.auth.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    public static final String CLAIM_TYPE = "type";
    public static final String CLAIM_ROLE = "role";

    public static final String TYPE_USER = "USER";
    public static final String TYPE_EMPLOYEE = "EMPLOYEE";
    public static final String TYPE_RIDER = "RIDER";

    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String createUserToken(long userId, String username) {
        return buildToken(TYPE_USER, userId, username, null);
    }

    public String createEmployeeToken(long employeeId, String username, String roleCode) {
        return buildToken(TYPE_EMPLOYEE, employeeId, username, roleCode);
    }

    public String createRiderToken(long riderId, String username) {
        return buildToken(TYPE_RIDER, riderId, username, null);
    }

    private String buildToken(String type, long subjectId, String username, String roleCode) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtProperties.getExpirationMs());
        var builder = Jwts.builder()
                .subject(String.valueOf(subjectId))
                .claim(CLAIM_TYPE, type)
                .claim("username", username)
                .issuedAt(now)
                .expiration(exp)
                .signWith(signingKey());
        if (roleCode != null) {
            builder.claim(CLAIM_ROLE, roleCode);
        }
        return builder.compact();
    }

    public JwtPrincipal parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        String type = claims.get(CLAIM_TYPE, String.class);
        long id = Long.parseLong(claims.getSubject());
        String username = claims.get("username", String.class);
        String roleCode = claims.get(CLAIM_ROLE, String.class);
        return new JwtPrincipal(type, id, username, roleCode);
    }

    private SecretKey signingKey() {
        byte[] bytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            bytes = padTo32(bytes);
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    private static byte[] padTo32(byte[] input) {
        byte[] out = new byte[32];
        System.arraycopy(input, 0, out, 0, Math.min(input.length, 32));
        return out;
    }

    public record JwtPrincipal(String type, long id, String username, String roleCode) {}
}
