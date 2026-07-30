package com.cinema.booking.seat.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

// Toan bo so do ghe cua 1 phong. Gui request nay se GHI DE toan bo ghe
// hien co cua phong (xoa het roi sinh lai) - hop ly vi Giai doan 1 chua
// co Showtime/Booking nao tham chieu seat_id cu the.
public record SeatLayoutRequest(
        @NotEmpty(message = "So do phai co it nhat 1 hang ghe")
        @Valid List<SeatRowRequest> rows
) {
}
