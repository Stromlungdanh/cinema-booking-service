package com.cinema.booking.cinema;

import com.cinema.booking.cinema.dto.CinemaRequest;
import com.cinema.booking.cinema.dto.CinemaResponse;
import com.cinema.booking.common.exception.ResourceNotFoundException;
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

@WebMvcTest(CinemaController.class)
class CinemaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CinemaService cinemaService;

    private CinemaRequest validRequest() {
        return new CinemaRequest(1L, "BHD Star Bitexco", "Quan 1", "Ho Chi Minh");
    }

    private CinemaResponse sampleResponse(Long id) {
        return new CinemaResponse(id, 1L, "BHD Star", "BHD Star Bitexco", "Quan 1", "Ho Chi Minh");
    }

    @Test
    void create_returns201WithBody() throws Exception {
        when(cinemaService.create(any())).thenReturn(sampleResponse(1L));

        mockMvc.perform(post("/api/admin/cinemas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.brandName").value("BHD Star"));
    }

    @Test
    void create_returns400WhenNameBlank() throws Exception {
        CinemaRequest invalid = new CinemaRequest(1L, "", null, null);

        mockMvc.perform(post("/api/admin/cinemas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void findById_returns404WhenMissing() throws Exception {
        when(cinemaService.findById(eq(99L)))
                .thenThrow(new ResourceNotFoundException("Khong tim thay rap voi id=99"));

        mockMvc.perform(get("/api/admin/cinemas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Khong tim thay rap voi id=99"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/admin/cinemas/1"))
                .andExpect(status().isNoContent());
    }
}
