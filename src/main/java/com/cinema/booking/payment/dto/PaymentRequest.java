package com.cinema.booking.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotNull(message = "bookingId khong duoc de trong") Long bookingId,
        @NotBlank(message = "idempotencyKey khong duoc de trong") String idempotencyKey
) {
}
