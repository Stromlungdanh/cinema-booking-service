package com.cinema.booking.payment;

import com.cinema.booking.common.util.QrCodeGenerator;
import com.cinema.booking.payment.dto.PaymentResponse;

public final class PaymentMapper {

    private PaymentMapper() {
    }

    public static PaymentResponse toResponse(Payment payment) {
        String ticketCode = payment.getBooking().getTicketCode();
        String qrCodeBase64 = ticketCode == null ? null : QrCodeGenerator.toBase64Png(ticketCode);
        return new PaymentResponse(
                payment.getId(),
                payment.getBooking().getId(),
                payment.getStatus(),
                payment.getMethod(),
                payment.getTransactionRef(),
                payment.getIdempotencyKey(),
                payment.getCreatedAt(),
                ticketCode,
                qrCodeBase64
        );
    }
}
