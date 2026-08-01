package com.cinema.booking.showtime.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record ShowtimeSeatMapResponse(
        Long showtimeId,
        Long movieId,
        String movieTitle,
        Long roomId,
        String roomName,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        BigDecimal basePrice,
        List<ShowtimeSeatResponse> seats
) {
}
