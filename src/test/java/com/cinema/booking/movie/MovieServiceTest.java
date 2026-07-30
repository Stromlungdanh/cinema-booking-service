package com.cinema.booking.movie;

import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.movie.dto.MovieRequest;
import com.cinema.booking.movie.dto.MovieResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Test thuan Mockito (khong @SpringBootTest) - khong can Postgres dang chay,
// giong tinh than cua HealthControllerTest. Day la noi nen viet nhieu test nhat
// vi day la lop chua business logic.
@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    private MovieRequest sampleRequest() {
        return new MovieRequest(
                "Avatar 3",
                "Mo ta phim",
                180,
                "English",
                LocalDate.of(2026, 12, 20),
                "http://poster.jpg",
                "http://trailer.mp4",
                MovieStatus.COMING_SOON
        );
    }

    @Test
    void create_savesEntityBuiltFromRequest() {
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie movie = invocation.getArgument(0);
            movie.setId(1L);
            return movie;
        });

        MovieResponse response = movieService.create(sampleRequest());

        ArgumentCaptor<Movie> captor = ArgumentCaptor.forClass(Movie.class);
        verify(movieRepository).save(captor.capture());
        assertEquals("Avatar 3", captor.getValue().getTitle());
        assertEquals(1L, response.id());
        assertEquals(MovieStatus.COMING_SOON, response.status());
    }

    @Test
    void findById_throwsResourceNotFoundWhenMissing() {
        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.findById(99L));
    }

    @Test
    void update_appliesNewValuesToExistingEntity() {
        Movie existing = new Movie();
        existing.setId(5L);
        existing.setTitle("Ten cu");
        when(movieRepository.findById(5L)).thenReturn(Optional.of(existing));

        MovieResponse response = movieService.update(5L, sampleRequest());

        assertEquals("Avatar 3", response.title());
        assertEquals(5L, response.id());
    }

    @Test
    void delete_removesExistingEntity() {
        Movie existing = new Movie();
        existing.setId(7L);
        when(movieRepository.findById(7L)).thenReturn(Optional.of(existing));

        movieService.delete(7L);

        verify(movieRepository).delete(existing);
    }

    @Test
    void delete_throwsResourceNotFoundWhenMissing() {
        when(movieRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> movieService.delete(404L));
    }
}
