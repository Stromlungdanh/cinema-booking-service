package com.cinema.booking.booking;

import com.cinema.booking.booking.dto.BookingRequest;
import com.cinema.booking.booking.dto.BookingResponse;
import com.cinema.booking.booking.dto.TicketResponse;
import com.cinema.booking.common.exception.BookingConflictException;
import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.movie.Movie;
import com.cinema.booking.room.Room;
import com.cinema.booking.seat.Seat;
import com.cinema.booking.seat.SeatRepository;
import com.cinema.booking.seattype.SeatType;
import com.cinema.booking.showtime.Showtime;
import com.cinema.booking.showtime.ShowtimeRepository;
import com.cinema.booking.user.User;
import com.cinema.booking.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private BookingSeatRepository bookingSeatRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private SeatRepository seatRepository;

    @InjectMocks
    private BookingService bookingService;

    private User user(long id) {
        User user = new User();
        user.setId(id);
        user.setName("Nguyen Van A");
        return user;
    }

    private Room room(long id, String name) {
        Room room = new Room();
        room.setId(id);
        room.setName(name);
        return room;
    }

    private Showtime showtime(long id, Room room, String basePrice) {
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Avengers");
        Showtime showtime = new Showtime();
        showtime.setId(id);
        showtime.setMovie(movie);
        showtime.setRoom(room);
        showtime.setBasePrice(new BigDecimal(basePrice));
        return showtime;
    }

    private SeatType seatType(long id, String name, String priceMultiplier) {
        SeatType seatType = new SeatType();
        seatType.setId(id);
        seatType.setName(name);
        seatType.setPriceMultiplier(new BigDecimal(priceMultiplier));
        return seatType;
    }

    private Seat seat(long id, Room room, String rowLabel, int colNumber, SeatType seatType) {
        Seat seat = new Seat();
        seat.setId(id);
        seat.setRoom(room);
        seat.setRowLabel(rowLabel);
        seat.setColNumber(colNumber);
        seat.setSeatType(seatType);
        return seat;
    }

    @Test
    void create_savesBookingWithCalculatedTotalPrice() {
        Room room = room(1L, "Phong 1");
        Showtime showtime = showtime(1L, room, "90000");
        SeatType standard = seatType(1L, "Standard", "1.00");
        SeatType vip = seatType(2L, "VIP", "1.50");
        Seat seatA1 = seat(1L, room, "A", 1, standard);
        Seat seatA2 = seat(2L, room, "A", 2, vip);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(seatRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(seatA1, seatA2));
        when(bookingSeatRepository.findBookedSeatIds(any(), anyList(), anyList())).thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(10L);
            return booking;
        });

        BookingRequest request = new BookingRequest(1L, 1L, List.of(1L, 2L));
        BookingResponse response = bookingService.create(request);

        assertEquals(10L, response.id());
        assertEquals(BookingStatus.PENDING, response.status());
        assertEquals(0, new BigDecimal("225000.00").compareTo(response.totalPrice()));
        assertEquals(2, response.seats().size());

        ArgumentCaptor<Booking> captor = ArgumentCaptor.forClass(Booking.class);
        verify(bookingRepository).save(captor.capture());
        assertEquals(2, captor.getValue().getSeats().size());
    }

    @Test
    void create_throwsWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        BookingRequest request = new BookingRequest(99L, 1L, List.of(1L));

        assertThrows(ResourceNotFoundException.class, () -> bookingService.create(request));
    }

    @Test
    void create_throwsWhenShowtimeNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(showtimeRepository.findById(99L)).thenReturn(Optional.empty());

        BookingRequest request = new BookingRequest(1L, 99L, List.of(1L));

        assertThrows(ResourceNotFoundException.class, () -> bookingService.create(request));
    }

    @Test
    void create_throwsWhenSeatDoesNotBelongToShowtimeRoom() {
        Room room = room(1L, "Phong 1");
        Room otherRoom = room(2L, "Phong 2");
        Showtime showtime = showtime(1L, room, "90000");
        Seat seatInOtherRoom = seat(5L, otherRoom, "A", 1, seatType(1L, "Standard", "1.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(seatRepository.findAllById(List.of(5L))).thenReturn(List.of(seatInOtherRoom));

        BookingRequest request = new BookingRequest(1L, 1L, List.of(5L));

        assertThrows(ResourceNotFoundException.class, () -> bookingService.create(request));
    }

    @Test
    void create_throwsBookingConflictWhenSeatAlreadyBooked() {
        Room room = room(1L, "Phong 1");
        Showtime showtime = showtime(1L, room, "90000");
        Seat seat = seat(1L, room, "A", 1, seatType(1L, "Standard", "1.00"));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(seatRepository.findAllById(List.of(1L))).thenReturn(List.of(seat));
        when(bookingSeatRepository.findBookedSeatIds(any(), anyList(), anyList())).thenReturn(List.of(1L));

        BookingRequest request = new BookingRequest(1L, 1L, List.of(1L));

        assertThrows(BookingConflictException.class, () -> bookingService.create(request));
    }

    @Test
    void findById_throwsWhenMissing() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookingService.findById(99L));
    }

    @Test
    void findByUser_throwsWhenUserNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> bookingService.findByUser(99L));
    }

    @Test
    void findByUser_returnsBookingsOrderedByCreatedAtDesc() {
        Room room = room(1L, "Phong 1");
        Showtime showtime = showtime(1L, room, "90000");
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user(1L));
        booking.setShowtime(showtime);
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(new BigDecimal("90000"));
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(booking));

        List<BookingResponse> responses = bookingService.findByUser(1L);

        assertEquals(1, responses.size());
        assertEquals(1L, responses.get(0).id());
    }

    @Test
    void cancel_setsStatusToCancelledWhenPending() {
        Room room = room(1L, "Phong 1");
        Showtime showtime = showtime(1L, room, "90000");
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user(1L));
        booking.setShowtime(showtime);
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(new BigDecimal("90000"));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        BookingResponse response = bookingService.cancel(1L);

        assertEquals(BookingStatus.CANCELLED, response.status());
    }

    @Test
    void cancel_throwsWhenBookingNotPending() {
        Room room = room(1L, "Phong 1");
        Showtime showtime = showtime(1L, room, "90000");
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user(1L));
        booking.setShowtime(showtime);
        booking.setStatus(BookingStatus.PAID);
        booking.setTotalPrice(new BigDecimal("90000"));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BookingConflictException.class, () -> bookingService.cancel(1L));
    }

    @Test
    void getTicket_returnsTicketWhenPaidWithTicketCode() {
        Room room = room(1L, "Phong 1");
        Showtime showtime = showtime(1L, room, "90000");
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user(1L));
        booking.setShowtime(showtime);
        booking.setStatus(BookingStatus.PAID);
        booking.setTotalPrice(new BigDecimal("90000"));
        booking.setTicketCode("CB-ABCD1234");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        TicketResponse ticket = bookingService.getTicket(1L);

        assertEquals("CB-ABCD1234", ticket.ticketCode());
        assertEquals(1L, ticket.bookingId());
        assertEquals("Avengers", ticket.movieTitle());
    }

    @Test
    void getTicket_throwsWhenBookingNotPaid() {
        Room room = room(1L, "Phong 1");
        Showtime showtime = showtime(1L, room, "90000");
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setUser(user(1L));
        booking.setShowtime(showtime);
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(new BigDecimal("90000"));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(BookingConflictException.class, () -> bookingService.getTicket(1L));
    }
}
