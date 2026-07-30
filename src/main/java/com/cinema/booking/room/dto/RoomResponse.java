package com.cinema.booking.room.dto;

public record RoomResponse(
        Long id,
        Long cinemaId,
        String cinemaName,
        String name,
        String roomType
) {
}
