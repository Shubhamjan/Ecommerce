package com.demo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;

    private final long jwtExpirationMs;

    public JwtUtil(@Value("${app.jwt.secret}")String secret,@Value("${app.jwt.expiration-ms}") long jwtExpirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.jwtExpirationMs = jwtExpirationMs;
    }


    public String generateToken(Long userId,String email,String role){
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email",email)
                .claim("role",role)
                .issuedAt(new Date(now))
                .expiration(new Date(now+jwtExpirationMs))
                .signWith(key)
                .compact();
    }

    public Jws<Claims> validateToken(String token){
       return Jwts.parser().verifyWith(key)
                .build()
                .parseSignedClaims(token);
    }

    public long getUserIdFromToken(String token){
        Claims claims = validateToken(token).getBody();
        return Long.valueOf(claims.getSubject());
    }

    public long getRemainingTime(String token) {

        Date expiry = getExpirationDate(token);

        return expiry.getTime() - System.currentTimeMillis();
    }

    public Date getExpirationDate(String token) {

        return  Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }
}
