package com.cinema.booking.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "name khong duoc de trong") String name,
        @NotBlank(message = "email khong duoc de trong") @Email(message = "email khong hop le") String email,
        @NotBlank(message = "password khong duoc de trong")
        @Size(min = 8, message = "password phai co it nhat 8 ky tu") String password
) {
}
