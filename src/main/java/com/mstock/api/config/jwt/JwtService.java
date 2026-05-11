package com.mstock.api.config.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationSeconds;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-seconds:3600}") long expirationSeconds) {

        this.signingKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8));

        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(UserDetails userDetails,
            Map<String, Object> extraClaims) {

        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(expirationSeconds);

        String subject = userDetails.getUsername();
        if (userDetails instanceof com.mstock.api.entities.User user && user.getEmail() != null) {
            subject = user.getEmail();
        }

        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public boolean isTokenValid(String token,
            UserDetails userDetails) {

        String username = extractUsername(token);
        boolean matches = username.equals(userDetails.getUsername());
        if (!matches && userDetails instanceof com.mstock.api.entities.User user) {
            matches = username.equals(user.getEmail())
                    || username.equals(user.getUsername());
        }

        return matches && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractAllClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
