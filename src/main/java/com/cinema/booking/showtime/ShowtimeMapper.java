package com.cinema.booking.showtime;

import com.cinema.booking.movie.Movie;
import com.cinema.booking.room.Room;
import com.cinema.booking.seat.Seat;
import com.cinema.booking.showtime.dto.SeatStatus;
import com.cinema.booking.showtime.dto.ShowtimeRequest;
import com.cinema.booking.showtime.dto.ShowtimeResponse;
import com.cinema.booking.showtime.dto.ShowtimeSeatMapResponse;
import com.cinema.booking.showtime.dto.ShowtimeSeatResponse;

import java.util.List;
import java.util.Set;

public final class ShowtimeMapper {

    private ShowtimeMapper() {
    }

    public static Showtime toEntity(Movie movie, Room room, ShowtimeRequest request) {
        Showtime showtime = new Showtime();
        applyRequest(showtime, movie, room, request);
        return showtime;
    }

    public static void applyRequest(Showtime showtime, Movie movie, Room room, ShowtimeRequest request) {
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setStartTime(request.startTime());
        showtime.setEndTime(request.endTime());
        showtime.setBasePrice(request.basePrice());
    }

    public static ShowtimeResponse toResponse(Showtime showtime) {
        return new ShowtimeResponse(
                showtime.getId(),
                showtime.getMovie().getId(),
                showtime.getMovie().getTitle(),
                showtime.getRoom().getId(),
                showtime.getRoom().getName(),
                showtime.getStartTime(),
                showtime.getEndTime(),
                showtime.getBasePrice()
        );
    }

    public static ShowtimeSeatMapResponse toSeatMapResponse(Showtime showtime, List<Seat> seats, Set<Long> bookedSeatIds) {
        List<ShowtimeSeatResponse> seatResponses = seats.stream()
                .map(seat -> new ShowtimeSeatResponse(
                        seat.getId(),
                        seat.getRowLabel(),
                        seat.getColNumber(),
                        seat.getSeatType().getId(),
                        seat.getSeatType().getName(),
                        showtime.getBasePrice().multiply(seat.getSeatType().getPriceMultiplier()),
                        bookedSeatIds.contains(seat.getId()) ? SeatStatus.BOOKED : SeatStatus.AVAILABLE
                ))
                .toList();
        return new ShowtimeSeatMapResponse(
                showtime.getId(),
                showtime.getMovie().getId(),
                showtime.getMovie().getTitle(),
                showtime.getRoom().getId(),
                showtime.getRoom().getName(),
                showtime.getStartTime(),
                showtime.getEndTime(),
                showtime.getBasePrice(),
                seatResponses
        );
    }
}
