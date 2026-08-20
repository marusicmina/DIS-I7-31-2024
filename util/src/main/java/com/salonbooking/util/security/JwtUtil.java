package com.salonbooking.util.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Zajednicka JWT logika - koristi je auth-service da izda token prilikom
 * logina, a kasnije ce je koristiti i gateway da validira token na ulazu u
 * sistem. Namerno ne izlaze io.jsonwebtoken tipove kroz svoj javni interfejs
 * (samo String/boolean/long) da potrosaci ovog modula ne moraju da imaju
 * jjwt na svom sopstvenom compile classpath-u.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:salon-booking-system-dev-secret-change-me-please-32chars-min}")
    private String secret;

    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;

    public String generateToken(long userId, String email, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey())
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public long extractUserId(String token) {
        // Broj se moze desirijalizovati kao Integer ili Long u zavisnosti od JSON parsera,
        // pa koristimo Number.longValue() umesto direktnog kastovanja da izbegnemo ClassCastException.
        Number userId = (Number) parseClaims(token).get("userId");
        return userId.longValue();
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
