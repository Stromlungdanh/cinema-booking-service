package com.cinema.booking.seat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// 1 hang ghe trong so do phong: nhan hang (VD "A") + so cot + loai ghe
// ap dung cho ca hang. Sinh cot theo so thu tu 1..columnCount.
public record SeatRowRequest(
        @NotBlank(message = "Nhan hang khong duoc de trong") String rowLabel,
        @NotNull(message = "So cot khong duoc de trong")
        @Positive(message = "So cot phai lon hon 0") Integer columnCount,
        @NotNull(message = "Loai ghe khong duoc de trong") Long seatTypeId
) {
}
