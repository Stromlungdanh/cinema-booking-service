package com.cinema.booking.genre;

import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.genre.dto.GenreRequest;
import com.cinema.booking.genre.dto.GenreResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GenreController.class)
class GenreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GenreService genreService;

    private GenreRequest validRequest() {
        return new GenreRequest("Hanh dong");
    }

    private GenreResponse sampleResponse(Long id) {
        return new GenreResponse(id, "Hanh dong");
    }

    @Test
    void create_returns201WithBody() throws Exception {
        when(genreService.create(any())).thenReturn(sampleResponse(1L));

        mockMvc.perform(post("/api/admin/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Hanh dong"));
    }

    @Test
    void create_returns400WhenNameBlank() throws Exception {
        GenreRequest invalid = new GenreRequest("");

        mockMvc.perform(post("/api/admin/genres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void findById_returns404WhenMissing() throws Exception {
        when(genreService.findById(eq(99L)))
                .thenThrow(new ResourceNotFoundException("Khong tim thay the loai voi id=99"));

        mockMvc.perform(get("/api/admin/genres/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Khong tim thay the loai voi id=99"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/admin/genres/1"))
                .andExpect(status().isNoContent());
    }
}
