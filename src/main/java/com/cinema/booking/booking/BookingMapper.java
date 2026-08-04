package com.cinema.booking.booking;

import com.cinema.booking.booking.dto.BookingResponse;
import com.cinema.booking.booking.dto.BookingSeatResponse;
import com.cinema.booking.booking.dto.TicketResponse;
import com.cinema.booking.common.util.QrCodeGenerator;
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
                booking.getTicketCode(),
                toSeatResponses(booking)
        );
    }

    // Ve chi hop le sau khi thanh toan (BookingService.getTicket da validate
    // status == PAID + ticketCode != null truoc khi goi ham nay), nen QR
    // luon sinh duoc tu ticketCode.
    public static TicketResponse toTicketResponse(Booking booking) {
        return new TicketResponse(
                booking.getId(),
                booking.getTicketCode(),
                QrCodeGenerator.toBase64Png(booking.getTicketCode()),
                booking.getShowtime().getMovie().getTitle(),
                booking.getShowtime().getRoom().getName(),
                booking.getShowtime().getStartTime(),
                toSeatResponses(booking)
        );
    }

    private static List<BookingSeatResponse> toSeatResponses(Booking booking) {
        return booking.getSeats().stream()
                .map(bookingSeat -> new BookingSeatResponse(
                        bookingSeat.getSeat().getId(),
                        bookingSeat.getSeat().getRowLabel(),
                        bookingSeat.getSeat().getColNumber(),
                        bookingSeat.getPrice()
                ))
                .toList();
    }
}
