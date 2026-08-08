package com.cinema.booking.showtime;

import com.cinema.booking.booking.BookingSeatRepository;
import com.cinema.booking.booking.BookingStatus;
import com.cinema.booking.cinema.CinemaRepository;
import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.movie.Movie;
import com.cinema.booking.movie.MovieRepository;
import com.cinema.booking.room.Room;
import com.cinema.booking.room.RoomRepository;
import com.cinema.booking.seat.Seat;
import com.cinema.booking.seat.SeatRepository;
import com.cinema.booking.seattype.SeatType;
import com.cinema.booking.showtime.dto.SeatStatus;
import com.cinema.booking.showtime.dto.ShowtimeRequest;
import com.cinema.booking.showtime.dto.ShowtimeResponse;
import com.cinema.booking.showtime.dto.ShowtimeSeatMapResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShowtimeServiceTest {

    @Mock
    private ShowtimeRepository showtimeRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private CinemaRepository cinemaRepository;

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private BookingSeatRepository bookingSeatRepository;

    @InjectMocks
    private ShowtimeService showtimeService;

    private Movie movie(long id, String title) {
        Movie movie = new Movie();
        movie.setId(id);
        movie.setTitle(title);
        return movie;
    }

    private Room room(long id, String name) {
        Room room = new Room();
        room.setId(id);
        room.setName(name);
        return room;
    }

    private SeatType seatType(long id, String name, String priceMultiplier) {
        SeatType seatType = new SeatType();
        seatType.setId(id);
        seatType.setName(name);
        seatType.setPriceMultiplier(new BigDecimal(priceMultiplier));
        return seatType;
    }

    private Seat seat(long id, String rowLabel, int colNumber, SeatType seatType) {
        Seat seat = new Seat();
        seat.setId(id);
        seat.setRowLabel(rowLabel);
        seat.setColNumber(colNumber);
        seat.setSeatType(seatType);
        return seat;
    }

    private ShowtimeRequest sampleRequest() {
        return new ShowtimeRequest(
                1L, 1L,
                OffsetDateTime.parse("2026-08-01T10:00:00+07:00"),
                OffsetDateTime.parse("2026-08-01T12:00:00+07:00"),
                new BigDecimal("90000")
        );
    }

    @Test
    void create_savesEntityBuiltFromRequest() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie(1L, "Avengers")));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room(1L, "Phong 1")));
        when(showtimeRepository.save(any(Showtime.class))).thenAnswer(invocation -> {
            Showtime showtime = invocation.getArgument(0);
            showtime.setId(1L);
            return showtime;
        });

        ShowtimeResponse response = showtimeService.create(sampleRequest());

        ArgumentCaptor<Showtime> captor = ArgumentCaptor.forClass(Showtime.class);
        verify(showtimeRepository).save(captor.capture());
        assertEquals(new BigDecimal("90000"), captor.getValue().getBasePrice());
        assertEquals(1L, response.id());
        assertEquals("Avengers", response.movieTitle());
        assertEquals("Phong 1", response.roomName());
    }

    @Test
    void create_throwsWhenMovieNotFound() {
        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        ShowtimeRequest request = new ShowtimeRequest(
                99L, 1L,
                OffsetDateTime.parse("2026-08-01T10:00:00+07:00"),
                OffsetDateTime.parse("2026-08-01T12:00:00+07:00"),
                new BigDecimal("90000")
        );

        assertThrows(ResourceNotFoundException.class, () -> showtimeService.create(request));
    }

    @Test
    void create_throwsWhenRoomNotFound() {
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie(1L, "Avengers")));
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());

        ShowtimeRequest request = new ShowtimeRequest(
                1L, 99L,
                OffsetDateTime.parse("2026-08-01T10:00:00+07:00"),
                OffsetDateTime.parse("2026-08-01T12:00:00+07:00"),
                new BigDecimal("90000")
        );

        assertThrows(ResourceNotFoundException.class, () -> showtimeService.create(request));
    }

    @Test
    void findById_throwsResourceNotFoundWhenMissing() {
        when(showtimeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> showtimeService.findById(99L));
    }

    @Test
    void update_appliesNewValuesToExistingEntity() {
        Showtime existing = new Showtime();
        existing.setId(5L);
        existing.setMovie(movie(1L, "Avengers"));
        existing.setRoom(room(1L, "Phong 1"));
        existing.setBasePrice(new BigDecimal("50000"));
        when(showtimeRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie(1L, "Avengers")));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room(1L, "Phong 1")));

        ShowtimeResponse response = showtimeService.update(5L, sampleRequest());

        assertEquals(new BigDecimal("90000"), response.basePrice());
        assertEquals(5L, response.id());
    }

    @Test
    void delete_removesExistingEntity() {
        Showtime existing = new Showtime();
        existing.setId(7L);
        when(showtimeRepository.findById(7L)).thenReturn(Optional.of(existing));

        showtimeService.delete(7L);

        verify(showtimeRepository).delete(existing);
    }

    @Test
    void delete_throwsResourceNotFoundWhenMissing() {
        when(showtimeRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> showtimeService.delete(404L));
    }

    @Test
    void findByCinemaAndDate_withoutMovieId_queriesByCinemaAndDateRangeOnly() {
        Showtime showtime = new Showtime();
        showtime.setId(1L);
        showtime.setMovie(movie(1L, "Avengers"));
        showtime.setRoom(room(1L, "Phong 1"));
        showtime.setStartTime(OffsetDateTime.parse("2026-08-01T10:00:00+07:00"));
        showtime.setEndTime(OffsetDateTime.parse("2026-08-01T12:00:00+07:00"));
        showtime.setBasePrice(new BigDecimal("90000"));
        when(cinemaRepository.existsById(1L)).thenReturn(true);
        when(showtimeRepository.findByRoom_Cinema_IdAndStartTimeBetween(any(), any(), any()))
                .thenReturn(List.of(showtime));

        List<ShowtimeResponse> response = showtimeService.findByCinemaAndDate(1L, LocalDate.of(2026, 8, 1), null);

        assertEquals(1, response.size());
        assertEquals("Avengers", response.get(0).movieTitle());
    }

    @Test
    void findByCinemaAndDate_withMovieId_queriesByCinemaMovieAndDateRange() {
        when(cinemaRepository.existsById(1L)).thenReturn(true);
        when(showtimeRepository.findByRoom_Cinema_IdAndMovie_IdAndStartTimeBetween(any(), any(), any(), any()))
                .thenReturn(List.of());

        List<ShowtimeResponse> response = showtimeService.findByCinemaAndDate(1L, LocalDate.of(2026, 8, 1), 1L);

        assertEquals(0, response.size());
        verify(showtimeRepository).findByRoom_Cinema_IdAndMovie_IdAndStartTimeBetween(any(), any(), any(), any());
    }

    @Test
    void findByCinemaAndDate_throwsWhenCinemaNotFound() {
        when(cinemaRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> showtimeService.findByCinemaAndDate(99L, LocalDate.of(2026, 8, 1), null));
    }

    @Test
    void getSeatMap_computesPriceFromBasePriceAndSeatTypeMultiplier() {
        Showtime showtime = new Showtime();
        showtime.setId(1L);
        showtime.setMovie(movie(1L, "Avengers"));
        showtime.setRoom(room(1L, "Phong 1"));
        showtime.setBasePrice(new BigDecimal("90000"));
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(seatRepository.findByRoomIdOrderByRowLabelAscColNumberAsc(1L))
                .thenReturn(List.of(seat(1L, "A", 1, seatType(1L, "VIP", "1.50"))));
        when(bookingSeatRepository.findBookedSeatIds(eq(1L), any(), any())).thenReturn(List.of());

        ShowtimeSeatMapResponse response = showtimeService.getSeatMap(1L);

        assertEquals(1, response.seats().size());
        assertEquals(0, new BigDecimal("135000.00").compareTo(response.seats().get(0).price()));
        assertEquals("VIP", response.seats().get(0).seatTypeName());
        assertEquals(SeatStatus.AVAILABLE, response.seats().get(0).status());
    }

    @Test
    void getSeatMap_marksAlreadyBookedSeatAsBooked() {
        Showtime showtime = new Showtime();
        showtime.setId(1L);
        showtime.setMovie(movie(1L, "Avengers"));
        showtime.setRoom(room(1L, "Phong 1"));
        showtime.setBasePrice(new BigDecimal("90000"));
        SeatType vip = seatType(1L, "VIP", "1.50");
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime));
        when(seatRepository.findByRoomIdOrderByRowLabelAscColNumberAsc(1L))
                .thenReturn(List.of(seat(1L, "A", 1, vip), seat(2L, "A", 2, vip)));
        when(bookingSeatRepository.findBookedSeatIds(eq(1L), any(), any())).thenReturn(List.of(2L));

        ShowtimeSeatMapResponse response = showtimeService.getSeatMap(1L);

        assertEquals(SeatStatus.AVAILABLE, response.seats().get(0).status());
        assertEquals(SeatStatus.BOOKED, response.seats().get(1).status());
    }

    @Test
    void getSeatMap_throwsWhenShowtimeNotFound() {
        when(showtimeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> showtimeService.getSeatMap(99L));
    }
}
