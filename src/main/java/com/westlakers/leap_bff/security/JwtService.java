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
    @Value("${jwt.shared-secret}")
    private String sharedSecret;

    public String generateToken(String email) {

        SecretKey key = Keys.hmacShaKeyFor(
            sharedSecret.getBytes(StandardCharsets.UTF_8));
        
        return Jwts.builder()
            .subject(email)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 3600000)) // 1 hour expiration
            .signWith(key)
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(
                sharedSecret.getBytes(StandardCharsets.UTF_8));
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
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
}
