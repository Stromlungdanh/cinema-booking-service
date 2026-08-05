package com.cinema.booking.user.dto;

import com.cinema.booking.user.UserRole;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull(message = "role khong duoc de trong") UserRole role
) {
}
