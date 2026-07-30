package com.cinema.booking.seat.dto;

public record SeatResponse(
        Long id,
        String rowLabel,
        Integer colNumber,
        Long seatTypeId,
        String seatTypeName
) {
}
