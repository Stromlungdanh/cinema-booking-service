package com.cinema.booking.room.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RoomRequest(
        @NotNull(message = "Rap chieu khong duoc de trong") Long cinemaId,
        @NotBlank(message = "Ten phong khong duoc de trong") String name,
        String roomType
) {
}
