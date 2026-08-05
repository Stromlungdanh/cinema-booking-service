package com.cinema.booking.user.dto;

import com.cinema.booking.user.UserRole;

import java.time.OffsetDateTime;

public record UserResponse(
        Long id,
        String name,
        String email,
        String provider,
        UserRole role,
        Boolean active,
        OffsetDateTime createdAt
) {
}
