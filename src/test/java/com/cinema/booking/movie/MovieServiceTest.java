package com.cinema.booking.movie;

import com.cinema.booking.actor.Actor;
import com.cinema.booking.actor.ActorRepository;
import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.genre.Genre;
import com.cinema.booking.genre.GenreRepository;
import com.cinema.booking.movie.dto.MovieCastRequest;
import com.cinema.booking.movie.dto.MovieRequest;
import com.cinema.booking.movie.dto.MovieResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    @Mock
    private GenreRepository genreRepository;

    @Mock
    private ActorRepository actorRepository;

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
                MovieStatus.COMING_SOON,
                null,
                null
        );
    }

    private Genre genre(long id, String name) {
        Genre genre = new Genre();
        genre.setId(id);
        genre.setName(name);
        return genre;
    }

    private Actor actor(long id, String name) {
        Actor actor = new Actor();
        actor.setId(id);
        actor.setName(name);
        return actor;
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
        assertTrue(response.genres().isEmpty());
        assertTrue(response.cast().isEmpty());
    }

    @Test
    void create_resolvesGenresAndCast() {
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie movie = invocation.getArgument(0);
            movie.setId(1L);
            return movie;
        });
        when(genreRepository.findAllById(List.of(10L))).thenReturn(List.of(genre(10L, "Hanh dong")));
        when(actorRepository.findAllById(List.of(20L))).thenReturn(List.of(actor(20L, "Sam Worthington")));

        MovieRequest request = new MovieRequest(
                "Avatar 3", "Mo ta", 180, "English", LocalDate.of(2026, 12, 20),
                null, null, MovieStatus.COMING_SOON,
                List.of(10L), List.of(new MovieCastRequest(20L, "Jake Sully"))
        );

        MovieResponse response = movieService.create(request);

        assertEquals(1, response.genres().size());
        assertEquals("Hanh dong", response.genres().get(0).name());
        assertEquals(1, response.cast().size());
        assertEquals("Sam Worthington", response.cast().get(0).actorName());
        assertEquals("Jake Sully", response.cast().get(0).roleName());
    }

    @Test
    void create_throwsWhenGenreIdNotFound() {
        when(movieRepository.save(any(Movie.class))).thenAnswer(invocation -> {
            Movie movie = invocation.getArgument(0);
            movie.setId(1L);
            return movie;
        });
        when(genreRepository.findAllById(List.of(99L))).thenReturn(List.of());

        MovieRequest request = new MovieRequest(
                "Avatar 3", "Mo ta", 180, "English", LocalDate.of(2026, 12, 20),
                null, null, MovieStatus.COMING_SOON, List.of(99L), null
        );

        assertThrows(ResourceNotFoundException.class, () -> movieService.create(request));
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
    void update_replacesExistingCastAndGenres() {
        Movie existing = new Movie();
        existing.setId(5L);
        existing.getGenres().add(genre(1L, "The loai cu"));
        existing.getCast().add(MovieMapper.toMovieCast(existing, actor(2L, "Dien vien cu"),
                new MovieCastRequest(2L, "Vai cu")));
        when(movieRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(genreRepository.findAllById(List.of(10L))).thenReturn(List.of(genre(10L, "The loai moi")));
        when(actorRepository.findAllById(List.of(20L))).thenReturn(List.of(actor(20L, "Dien vien moi")));

        MovieRequest request = new MovieRequest(
                "Avatar 3", "Mo ta", 180, "English", LocalDate.of(2026, 12, 20),
                null, null, MovieStatus.COMING_SOON,
                List.of(10L), List.of(new MovieCastRequest(20L, "Vai moi"))
        );

        MovieResponse response = movieService.update(5L, request);

        assertEquals(1, response.genres().size());
        assertEquals("The loai moi", response.genres().get(0).name());
        assertEquals(1, response.cast().size());
        assertEquals("Dien vien moi", response.cast().get(0).actorName());
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
