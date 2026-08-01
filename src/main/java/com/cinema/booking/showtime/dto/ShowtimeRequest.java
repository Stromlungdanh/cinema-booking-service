package com.cinema.booking.showtime.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ShowtimeRequest(
        @NotNull(message = "Phim khong duoc de trong") Long movieId,
        @NotNull(message = "Phong chieu khong duoc de trong") Long roomId,
        @NotNull(message = "Gio bat dau khong duoc de trong") OffsetDateTime startTime,
        @NotNull(message = "Gio ket thuc khong duoc de trong") OffsetDateTime endTime,
        @NotNull(message = "Gia ve khong duoc de trong")
        @Positive(message = "Gia ve phai lon hon 0") BigDecimal basePrice
) {
}
