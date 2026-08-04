package com.cinema.booking.payment.dto;

import com.cinema.booking.payment.PaymentStatus;

import java.time.OffsetDateTime;

public record PaymentResponse(
        Long id,
        Long bookingId,
        PaymentStatus status,
        String method,
        String transactionRef,
        String idempotencyKey,
        OffsetDateTime createdAt,
        String ticketCode,
        String qrCodeBase64
) {
}
