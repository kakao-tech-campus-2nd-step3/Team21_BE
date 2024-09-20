package com.potatocake.everymoment.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.Optional;
import javax.crypto.SecretKey;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    private final SecretKey SECRET_KEY = Jwts.SIG.HS256.key().build();
    private final Long EXPIRE = 1000L * 60 * 60 * 48;
    public final String PREFIX = "Bearer ";

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
