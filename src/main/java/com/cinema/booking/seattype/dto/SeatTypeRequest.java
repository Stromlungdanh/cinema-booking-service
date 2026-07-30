package com.cinema.booking.seattype.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SeatTypeRequest(
        @NotBlank(message = "Ten loai ghe khong duoc de trong")
        @Size(max = 50) String name,
        @NotNull(message = "He so gia khong duoc de trong")
        @Positive(message = "He so gia phai lon hon 0") BigDecimal priceMultiplier
) {
}
