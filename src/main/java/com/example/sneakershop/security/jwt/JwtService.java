package com.example.sneakershop.security.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtService {
    private final static Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512); // In production, use a secure key and store it safely
    private final static long EXPIRATION_TIME = 86400000; // 1 day in milliseconds

    public static String generateToken(String email) {
        // Here you would use a library like io.jsonwebtoken.Jwts to create a JWT token
        // For example:
         return Jwts.builder()
                 .setSubject(email)
                 .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                 .signWith(key)
                 .compact();
    }

    public String extractEmail(String token) {
        // Extract the email (subject) from the token
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false; // Token is invalid or expired
        }
    }
}
