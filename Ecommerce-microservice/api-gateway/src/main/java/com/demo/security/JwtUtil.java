package com.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey key;


    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public Claims extractClaims(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
    }

    public boolean validateToken(String token){
        try{
            Claims claims = extractClaims(token);
            Date expiration = claims.getExpiration();
            return expiration != null && expiration.after(new Date());
        }catch (Exception e){
            return false;
        }
    }

    public String getUserId(String token){
        return extractClaims(token).getSubject();
    }

    public String getRole(String token){
        return extractClaims(token).get("role",String.class);
    }

    public String getEmail(String token){
        return extractClaims(token).get("email",String.class);
    }
}
