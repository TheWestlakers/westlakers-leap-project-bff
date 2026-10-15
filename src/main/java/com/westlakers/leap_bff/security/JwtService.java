package com.westlakers.leap_bff.security;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Claims;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;


@Service
public class JwtService {
    private static final long ACCESS_TOKEN_EXPIRATION_MS = 15 * 60 * 1000L;
    private static final long REFRESH_TOKEN_EXPIRATION_MS = 7 * 24 * 60 * 60 * 1000L;
    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    @Value("${jwt.shared-secret}")
    private String sharedSecret;

    public String generateToken(String email) {

        return generateToken(email, ACCESS_TOKEN_EXPIRATION_MS, null);
    }

    public String generateRefreshToken(String email) {
        return generateToken(email, REFRESH_TOKEN_EXPIRATION_MS, REFRESH_TOKEN_TYPE);
    }

    public boolean validateRefreshToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(
                sharedSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            return REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
        } catch (Exception e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(
            sharedSecret.getBytes(StandardCharsets.UTF_8));
        Claims claims = Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return claims.getSubject();
    }

    private String generateToken(String email, long expirationMs, String tokenType) {

        SecretKey key = Keys.hmacShaKeyFor(
            sharedSecret.getBytes(StandardCharsets.UTF_8));

        var builder = Jwts.builder()
            .subject(email)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationMs));
        if (tokenType != null) {
            builder.claim(TOKEN_TYPE_CLAIM, tokenType);
        }
        return builder.signWith(key).compact();
    }

    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(
                sharedSecret.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            return !REFRESH_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
        } catch (Exception e) {
            return false;
        }
    }

}
