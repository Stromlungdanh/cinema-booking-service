package com.cinema.booking.security;

import com.cinema.booking.user.User;
import com.cinema.booking.user.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-at-least-32-bytes-long!!";

    private User user(long id, String email, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }

    @Test
    void generateToken_thenParseClaims_roundTripsUserInfo() {
        JwtService jwtService = new JwtService(SECRET, 60_000);
        User user = user(1L, "user@example.com", UserRole.ADMIN);

        String token = jwtService.generateToken(user);
        Claims claims = jwtService.parseClaims(token);

        assertEquals("1", claims.getSubject());
        assertEquals("user@example.com", claims.get("email", String.class));
        assertEquals("ADMIN", claims.get("role", String.class));
    }

    @Test
    void parseClaims_throwsWhenTokenExpired() throws InterruptedException {
        JwtService jwtService = new JwtService(SECRET, 1);
        String token = jwtService.generateToken(user(1L, "user@example.com", UserRole.USER));

        Thread.sleep(10);

        assertThrows(JwtException.class, () -> jwtService.parseClaims(token));
    }

    @Test
    void parseClaims_throwsWhenSignedWithDifferentSecret() {
        JwtService issuer = new JwtService(SECRET, 60_000);
        JwtService verifier = new JwtService("a-completely-different-secret-key-32bytes", 60_000);
        String token = issuer.generateToken(user(1L, "user@example.com", UserRole.USER));

        assertThrows(JwtException.class, () -> verifier.parseClaims(token));
    }
}
