package com.cinema.booking.booking.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record TicketResponse(
        Long bookingId,
        String ticketCode,
        String qrCodeBase64,
        String movieTitle,
        String roomName,
        OffsetDateTime startTime,
        List<BookingSeatResponse> seats
) {
}
