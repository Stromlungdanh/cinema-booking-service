package com.cinema.booking.showtime.dto;

import java.math.BigDecimal;

public record ShowtimeSeatResponse(
        Long id,
        String rowLabel,
        Integer colNumber,
        Long seatTypeId,
        String seatTypeName,
        BigDecimal price,
        SeatStatus status
) {
}
