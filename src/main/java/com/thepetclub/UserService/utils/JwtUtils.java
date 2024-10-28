package com.thepetclub.UserService.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

    @Getter
    Date expirationTime = new Date(System.currentTimeMillis()  + 1000L * 60 * 60 * 60 * 24 * 7);


    @Value("${spring.secretKey}")
    private String secretKey;

    // Generate a signing key from the secret key
    private SecretKey getSigningKey() {
        return new SecretKeySpec(secretKey.getBytes(), SignatureAlgorithm.HS256.getJcaName());
    }

    // Extract the phone number (subject) from the token
    public String extractPhoneNumber(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    // Extract the expiration date from the token
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    // Extract all claims from the token
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .parseClaimsJws(token)
                .getBody();
    }

    // Generate a token for a given phone number
    public String generateToken(String phoneNumber) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, phoneNumber);
    }

    // Create the token with the provided claims and subject (phone number)
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expirationTime)  // 7 days expiration
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())  // Use secretKey.getBytes() for signing
                .compact();
    }

    // Check if the token is expired
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Validate the token by checking its expiration
    public boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

}
