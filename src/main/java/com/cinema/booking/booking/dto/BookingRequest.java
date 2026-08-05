package com.cinema.booking.booking.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BookingRequest(
        @NotNull(message = "showtimeId khong duoc de trong") Long showtimeId,
        @NotEmpty(message = "Phai chon it nhat 1 ghe") List<Long> seatIds
) {
}
