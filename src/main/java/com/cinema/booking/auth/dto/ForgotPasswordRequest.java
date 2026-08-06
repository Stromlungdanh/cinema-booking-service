package com.cinema.booking.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank(message = "email khong duoc de trong") @Email(message = "email khong hop le") String email
) {
}
