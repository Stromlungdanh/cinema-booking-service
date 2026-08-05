package com.cinema.booking.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "email khong duoc de trong") String email,
        @NotBlank(message = "password khong duoc de trong") String password
) {
}
