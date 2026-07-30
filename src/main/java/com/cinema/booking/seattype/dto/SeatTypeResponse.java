package com.cinema.booking.seattype.dto;

import java.math.BigDecimal;

public record SeatTypeResponse(
        Long id,
        String name,
        BigDecimal priceMultiplier
) {
}
