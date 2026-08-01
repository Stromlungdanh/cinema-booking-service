package com.cinema.booking.showtime;

import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.movie.Movie;
import com.cinema.booking.movie.MovieRepository;
import com.cinema.booking.room.Room;
import com.cinema.booking.room.RoomRepository;
import com.cinema.booking.showtime.dto.ShowtimeRequest;
import com.cinema.booking.showtime.dto.ShowtimeResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
}
