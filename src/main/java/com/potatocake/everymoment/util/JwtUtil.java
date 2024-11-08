package com.potatocake.everymoment.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey SECRET_KEY;
    private final Long EXPIRE = 1000L * 60 * 60 * 48;
    public final String PREFIX = "Bearer ";

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        this.SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);
    }

    public Long getId(String token) {
        return Jwts.parser().verifyWith(SECRET_KEY).build()
                .parseSignedClaims(token)
                .getPayload()
                .get("id", Long.class);
    }

    public boolean isExpired(String token) {
        try {
            return Jwts.parser().verifyWith(SECRET_KEY).build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration()
                    .before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    public String create(Long id) {
        return Jwts.builder()
                .claim("id", id)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRE))
                .signWith(SECRET_KEY)
                .compact();
    }

    public Optional<String> resolveToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .filter(auth -> auth.startsWith(PREFIX))
                .map(auth -> auth.split(" ")[1]);
    }

}
