package com.cinema.booking.room;

import com.cinema.booking.cinema.Cinema;
import com.cinema.booking.room.dto.RoomRequest;
import com.cinema.booking.room.dto.RoomResponse;

public final class RoomMapper {

    private RoomMapper() {
    }

    public static Room toEntity(Cinema cinema, RoomRequest request) {
        Room room = new Room();
        applyRequest(room, cinema, request);
        return room;
    }

    public static void applyRequest(Room room, Cinema cinema, RoomRequest request) {
        room.setCinema(cinema);
        room.setName(request.name());
        if (request.roomType() != null) {
            room.setRoomType(request.roomType());
        }
    }

    public static RoomResponse toResponse(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getCinema().getId(),
                room.getCinema().getName(),
                room.getName(),
                room.getRoomType()
        );
    }
}
