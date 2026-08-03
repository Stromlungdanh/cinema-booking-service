package com.cinema.booking.booking;

import com.cinema.booking.booking.dto.BookingResponse;
import com.cinema.booking.booking.dto.BookingSeatResponse;
import com.cinema.booking.seat.Seat;
import com.cinema.booking.showtime.Showtime;

import java.math.BigDecimal;
import java.util.List;

public final class BookingMapper {

    private BookingMapper() {
    }

    public static BigDecimal priceForSeat(Showtime showtime, Seat seat) {
        return showtime.getBasePrice().multiply(seat.getSeatType().getPriceMultiplier());
    }

    public static BookingResponse toResponse(Booking booking) {
        List<BookingSeatResponse> seatResponses = booking.getSeats().stream()
                .map(bookingSeat -> new BookingSeatResponse(
                        bookingSeat.getSeat().getId(),
                        bookingSeat.getSeat().getRowLabel(),
                        bookingSeat.getSeat().getColNumber(),
                        bookingSeat.getPrice()
                ))
                .toList();
        return new BookingResponse(
                booking.getId(),
                booking.getUser().getId(),
                booking.getShowtime().getId(),
                booking.getShowtime().getMovie().getTitle(),
                booking.getShowtime().getRoom().getName(),
                booking.getShowtime().getStartTime(),
                booking.getStatus(),
                booking.getTotalPrice(),
                booking.getCreatedAt(),
                seatResponses
        );
    }
}
