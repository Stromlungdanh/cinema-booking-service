package com.cinema.booking.seat;

import com.cinema.booking.room.Room;
import com.cinema.booking.seat.dto.SeatResponse;
import com.cinema.booking.seattype.SeatType;

public final class SeatMapper {

    private SeatMapper() {
    }

    public static Seat toEntity(Room room, SeatType seatType, String rowLabel, int colNumber) {
        Seat seat = new Seat();
        seat.setRoom(room);
        seat.setSeatType(seatType);
        seat.setRowLabel(rowLabel);
        seat.setColNumber(colNumber);
        return seat;
    }

    public static SeatResponse toResponse(Seat seat) {
        return new SeatResponse(
                seat.getId(),
                seat.getRowLabel(),
                seat.getColNumber(),
                seat.getSeatType().getId(),
                seat.getSeatType().getName()
        );
    }
}
