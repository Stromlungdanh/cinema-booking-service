package com.cinema.booking.showtime.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ShowtimeResponse(
        Long id,
        Long movieId,
        String movieTitle,
        Long roomId,
        String roomName,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        BigDecimal basePrice
) {
}
