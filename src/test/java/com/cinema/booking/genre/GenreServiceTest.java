package com.cinema.booking.genre;

import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.genre.dto.GenreRequest;
import com.cinema.booking.genre.dto.GenreResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GenreServiceTest {

    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private GenreService genreService;

    private GenreRequest sampleRequest() {
        return new GenreRequest("Hanh dong");
    }

    @Test
    void create_savesEntityBuiltFromRequest() {
        when(genreRepository.save(any(Genre.class))).thenAnswer(invocation -> {
            Genre genre = invocation.getArgument(0);
            genre.setId(1L);
            return genre;
        });

        GenreResponse response = genreService.create(sampleRequest());

        ArgumentCaptor<Genre> captor = ArgumentCaptor.forClass(Genre.class);
        verify(genreRepository).save(captor.capture());
        assertEquals("Hanh dong", captor.getValue().getName());
        assertEquals(1L, response.id());
    }

    @Test
    void findById_throwsResourceNotFoundWhenMissing() {
        when(genreRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> genreService.findById(99L));
    }

    @Test
    void update_appliesNewValuesToExistingEntity() {
        Genre existing = new Genre();
        existing.setId(5L);
        existing.setName("Ten cu");
        when(genreRepository.findById(5L)).thenReturn(Optional.of(existing));

        GenreResponse response = genreService.update(5L, sampleRequest());

        assertEquals("Hanh dong", response.name());
        assertEquals(5L, response.id());
    }

    @Test
    void delete_removesExistingEntity() {
        Genre existing = new Genre();
        existing.setId(7L);
        when(genreRepository.findById(7L)).thenReturn(Optional.of(existing));

        genreService.delete(7L);

        verify(genreRepository).delete(existing);
    }

    @Test
    void delete_throwsResourceNotFoundWhenMissing() {
        when(genreRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> genreService.delete(404L));
    }
}
