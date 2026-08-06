package com.cinema.booking.security;

public record GoogleUserInfo(
        String providerId,
        String email,
        String name,
        boolean emailVerified
) {
}
