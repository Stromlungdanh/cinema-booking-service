package com.cinema.booking.showtime;

import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.showtime.dto.ShowtimeRequest;
import com.cinema.booking.showtime.dto.ShowtimeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShowtimeController.class)
class ShowtimeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShowtimeService showtimeService;

    private ShowtimeRequest validRequest() {
        return new ShowtimeRequest(
                1L, 1L,
                OffsetDateTime.parse("2026-08-01T10:00:00+07:00"),
                OffsetDateTime.parse("2026-08-01T12:00:00+07:00"),
                new BigDecimal("90000")
        );
    }

    private ShowtimeResponse sampleResponse(Long id) {
        return new ShowtimeResponse(
                id, 1L, "Avengers", 1L, "Phong 1",
                OffsetDateTime.parse("2026-08-01T10:00:00+07:00"),
                OffsetDateTime.parse("2026-08-01T12:00:00+07:00"),
                new BigDecimal("90000")
        );
    }

    @Test
    void create_returns201WithBody() throws Exception {
        when(showtimeService.create(any())).thenReturn(sampleResponse(1L));

        mockMvc.perform(post("/api/admin/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.movieTitle").value("Avengers"));
    }

    @Test
    void create_returns400WhenBasePriceMissing() throws Exception {
        ShowtimeRequest invalid = new ShowtimeRequest(
                1L, 1L,
                OffsetDateTime.parse("2026-08-01T10:00:00+07:00"),
                OffsetDateTime.parse("2026-08-01T12:00:00+07:00"),
                null
        );

        mockMvc.perform(post("/api/admin/showtimes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.basePrice").exists());
    }

    @Test
    void findById_returns404WhenMissing() throws Exception {
        when(showtimeService.findById(eq(99L)))
                .thenThrow(new ResourceNotFoundException("Khong tim thay suat chieu voi id=99"));

        mockMvc.perform(get("/api/admin/showtimes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Khong tim thay suat chieu voi id=99"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/admin/showtimes/1"))
                .andExpect(status().isNoContent());
    }
}
