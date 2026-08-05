package com.cinema.booking.movie;

import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.movie.dto.MovieResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MoviePublicController.class)
@AutoConfigureMockMvc(addFilters = false)
class MoviePublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieService movieService;

    private MovieResponse sampleResponse(Long id) {
        return new MovieResponse(
                id, "Avatar 3", "Mo ta", 180, "English", LocalDate.of(2026, 12, 20),
                null, null, MovieStatus.NOW_SHOWING, 100L, List.of(), List.of()
        );
    }

    @Test
    void search_returns200WithBody() throws Exception {
        when(movieService.search(any(), any())).thenReturn(List.of(sampleResponse(1L)));

        mockMvc.perform(get("/api/movies").param("status", "NOW_SHOWING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Avatar 3"));
    }

    @Test
    void featured_returns200WithBody() throws Exception {
        when(movieService.findFeatured(anyInt())).thenReturn(List.of(sampleResponse(1L)));

        mockMvc.perform(get("/api/movies/featured").param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void findById_returns404WhenMissing() throws Exception {
        when(movieService.findById(eq(99L)))
                .thenThrow(new ResourceNotFoundException("Khong tim thay phim voi id=99"));

        mockMvc.perform(get("/api/movies/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Khong tim thay phim voi id=99"));
    }
}
