package com.cinema.booking.showtime;

import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.showtime.dto.ShowtimeResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShowtimePublicController.class)
class ShowtimePublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShowtimeService showtimeService;

    private ShowtimeResponse sampleResponse() {
        return new ShowtimeResponse(
                1L, 1L, "Avengers", 1L, "Phong 1",
                OffsetDateTime.parse("2026-08-01T10:00:00+07:00"),
                OffsetDateTime.parse("2026-08-01T12:00:00+07:00"),
                new BigDecimal("90000")
        );
    }

    @Test
    void findByCinemaAndDate_returns200WithBody() throws Exception {
        when(showtimeService.findByCinemaAndDate(eq(1L), any(), isNull())).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/cinemas/1/showtimes").param("date", "2026-08-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].movieTitle").value("Avengers"));
    }

    @Test
    void findByCinemaAndDate_returns404WhenCinemaMissing() throws Exception {
        when(showtimeService.findByCinemaAndDate(eq(99L), any(), isNull()))
                .thenThrow(new ResourceNotFoundException("Khong tim thay rap voi id=99"));

        mockMvc.perform(get("/api/cinemas/99/showtimes").param("date", "2026-08-01"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Khong tim thay rap voi id=99"));
    }
}
