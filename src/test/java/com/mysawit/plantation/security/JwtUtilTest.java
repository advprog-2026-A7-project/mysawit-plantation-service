package com.mysawit.plantation.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private static final String SECRET = "defaultSuperSecretKeyThatIsAtLeast32BytesLong";

    private final JwtUtil jwtUtil = new JwtUtil(SECRET);

    @Test
    void validateTokenAndExtractClaimsWorkForSignedToken() {
        String token = Jwts.builder()
                .subject("test-user")
                .claim("role", "ADMIN")
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertTrue(jwtUtil.validateToken(token));
        assertEquals("test-user", jwtUtil.extractClaims(token).getSubject());
        assertEquals("ADMIN", jwtUtil.extractClaims(token).get("role", String.class));
    }

    @Test
    void validateTokenReturnsFalseForInvalidToken() {
        assertFalse(jwtUtil.validateToken("not-a-jwt"));
    }
}
