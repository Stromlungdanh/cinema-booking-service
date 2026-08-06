package com.cinema.booking.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "token khong duoc de trong") String token,
        @NotBlank(message = "password khong duoc de trong")
        @Size(min = 8, message = "password phai co it nhat 8 ky tu") String newPassword
) {
}
