package com.cinema.booking.movie;

import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.movie.dto.MovieCastRequest;
import com.cinema.booking.movie.dto.MovieRequest;
import com.cinema.booking.movie.dto.MovieResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest chi load lop web (Controller + GlobalExceptionHandler), khong
// load JPA/Datasource -> khong can Postgres dang chay. MovieService bi mock
// hoan toan bang @MockBean.
@WebMvcTest(MovieController.class)
@AutoConfigureMockMvc(addFilters = false)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MovieService movieService;

    private MovieRequest validRequest() {
        return new MovieRequest(
                "Avatar 3", "Mo ta", 180, "English",
                LocalDate.of(2026, 12, 20), null, null, MovieStatus.COMING_SOON,
                List.of(1L), List.of(new MovieCastRequest(2L, "Jake Sully"))
        );
    }

    private MovieResponse sampleResponse(Long id) {
        return new MovieResponse(
                id, "Avatar 3", "Mo ta", 180, "English",
                LocalDate.of(2026, 12, 20), null, null, MovieStatus.COMING_SOON, 0L,
                List.of(), List.of()
        );
    }

    @Test
    void create_returns201WithBody() throws Exception {
        when(movieService.create(any())).thenReturn(sampleResponse(1L));

        mockMvc.perform(post("/api/admin/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Avatar 3"));
    }

    @Test
    void create_returns400WhenTitleBlank() throws Exception {
        MovieRequest invalid = new MovieRequest(
                "", "Mo ta", 180, "English",
                LocalDate.of(2026, 12, 20), null, null, MovieStatus.COMING_SOON,
                null, null
        );

        mockMvc.perform(post("/api/admin/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists());
    }

    @Test
    void findById_returns404WhenMissing() throws Exception {
        when(movieService.findById(eq(99L)))
                .thenThrow(new ResourceNotFoundException("Khong tim thay phim voi id=99"));

        mockMvc.perform(get("/api/admin/movies/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Khong tim thay phim voi id=99"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/admin/movies/1"))
                .andExpect(status().isNoContent());
    }
}
