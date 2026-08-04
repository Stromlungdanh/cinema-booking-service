package com.cinema.booking.booking.dto;

import com.cinema.booking.booking.BookingStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record BookingResponse(
        Long id,
        Long userId,
        Long showtimeId,
        String movieTitle,
        String roomName,
        OffsetDateTime startTime,
        BookingStatus status,
        BigDecimal totalPrice,
        OffsetDateTime createdAt,
        String ticketCode,
        List<BookingSeatResponse> seats
) {
}
