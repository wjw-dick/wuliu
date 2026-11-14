package com.quantaida.wuliu.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/** JWT 签发与校验。token 内含 uid/scope/tenantId/driverId/carrierId，服务端不信任前端传参。 */
@Component
public class JwtUtil {
    private final SecretKey key;
    private final Duration ttl;

    public JwtUtil(@Value("${app.jwt.secret:wl-demo-jwt-secret-please-change-in-prod-2026}") String secret,
                   @Value("${app.jwt.ttl-hours:12}") long ttlHours) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        byte[] normalized = new byte[32];
        for (int i = 0; i < 32; i++) {
            normalized[i] = bytes[i % bytes.length];
        }
        this.key = Keys.hmacShaKeyFor(normalized);
        this.ttl = Duration.ofHours(ttlHours);
    }

    public String issue(Long userId, String username, String scope, Long tenantId, Long driverId, Long carrierId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("scope", scope);
        if (tenantId != null) claims.put("tenantId", tenantId);
        if (driverId != null) claims.put("driverId", driverId);
        if (carrierId != null) claims.put("carrierId", carrierId);
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claims(claims)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ttl.toMillis()))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
