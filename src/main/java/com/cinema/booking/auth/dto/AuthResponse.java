package com.cinema.booking.auth.dto;

import com.cinema.booking.user.UserRole;

public record AuthResponse(
        String token,
        Long userId,
        String name,
        String email,
        UserRole role
) {
}
