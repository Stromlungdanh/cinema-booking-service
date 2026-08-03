package com.cinema.booking.booking.dto;

import java.math.BigDecimal;

public record BookingSeatResponse(
        Long seatId,
        String rowLabel,
        Integer colNumber,
        BigDecimal price
) {
}
