package com.cinema.booking.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequest(
        @NotBlank(message = "idToken khong duoc de trong") String idToken
) {
}
