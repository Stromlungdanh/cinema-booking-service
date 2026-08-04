package com.cinema.booking.payment;

import com.cinema.booking.booking.Booking;
import com.cinema.booking.booking.BookingRepository;
import com.cinema.booking.booking.BookingStatus;
import com.cinema.booking.common.exception.BookingConflictException;
import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.movie.Movie;
import com.cinema.booking.payment.dto.PaymentRequest;
import com.cinema.booking.payment.dto.PaymentResponse;
import com.cinema.booking.room.Room;
import com.cinema.booking.showtime.Showtime;
import com.cinema.booking.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Booking pendingBooking(long id) {
        Room room = new Room();
        room.setId(1L);
        room.setName("Phong 1");
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Avengers");
        Showtime showtime = new Showtime();
        showtime.setId(1L);
        showtime.setMovie(movie);
        showtime.setRoom(room);

        Booking booking = new Booking();
        booking.setId(id);
        booking.setUser(new User());
        booking.setShowtime(showtime);
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(new BigDecimal("90000"));
        return booking;
    }

    @Test
    void pay_marksBookingPaidAndGeneratesTicketCode() {
        Booking booking = pendingBooking(1L);
        when(paymentRepository.findByIdempotencyKey("idem-1")).thenReturn(Optional.empty());
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(100L);
            return payment;
        });

        PaymentResponse response = paymentService.pay(new PaymentRequest(1L, "idem-1"));

        assertEquals(PaymentStatus.SUCCESS, response.status());
        assertEquals(BookingStatus.PAID, booking.getStatus());
        assertNotNull(booking.getTicketCode());
        assertEquals(booking.getTicketCode(), response.ticketCode());
        assertNotNull(response.qrCodeBase64());
    }

    @Test
    void pay_returnsExistingPaymentWhenIdempotencyKeyReused() {
        Booking booking = pendingBooking(1L);
        booking.setStatus(BookingStatus.PAID);
        booking.setTicketCode("CB-EXISTING1");
        Payment existing = new Payment();
        existing.setId(50L);
        existing.setBooking(booking);
        existing.setStatus(PaymentStatus.SUCCESS);
        existing.setTransactionRef("FAKE-old");
        existing.setIdempotencyKey("idem-1");
        when(paymentRepository.findByIdempotencyKey("idem-1")).thenReturn(Optional.of(existing));

        PaymentResponse response = paymentService.pay(new PaymentRequest(1L, "idem-1"));

        assertEquals(50L, response.id());
        assertEquals("CB-EXISTING1", response.ticketCode());
    }

    @Test
    void pay_throwsWhenIdempotencyKeyUsedForDifferentBooking() {
        Booking otherBooking = pendingBooking(2L);
        Payment existing = new Payment();
        existing.setId(50L);
        existing.setBooking(otherBooking);
        existing.setIdempotencyKey("idem-1");
        when(paymentRepository.findByIdempotencyKey("idem-1")).thenReturn(Optional.of(existing));

        assertThrows(BookingConflictException.class,
                () -> paymentService.pay(new PaymentRequest(1L, "idem-1")));
    }

    @Test
    void pay_throwsWhenBookingNotFound() {
        when(paymentRepository.findByIdempotencyKey("idem-1")).thenReturn(Optional.empty());
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.pay(new PaymentRequest(99L, "idem-1")));
    }

    @Test
    void pay_throwsWhenBookingNotPending() {
        Booking booking = pendingBooking(1L);
        booking.setStatus(BookingStatus.CANCELLED);
        when(paymentRepository.findByIdempotencyKey("idem-1")).thenReturn(Optional.empty());
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BookingConflictException.class,
                () -> paymentService.pay(new PaymentRequest(1L, "idem-1")));
    }
}
