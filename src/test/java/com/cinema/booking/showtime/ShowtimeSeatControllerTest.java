package com.cinema.booking.showtime;

import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.showtime.dto.SeatStatus;
import com.cinema.booking.showtime.dto.ShowtimeSeatMapResponse;
import com.cinema.booking.showtime.dto.ShowtimeSeatResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShowtimeSeatController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShowtimeSeatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ShowtimeService showtimeService;

    @Test
    void getSeatMap_returns200WithBody() throws Exception {
        ShowtimeSeatMapResponse response = new ShowtimeSeatMapResponse(
                1L, 1L, "Avengers", 1L, "Phong 1",
                OffsetDateTime.parse("2026-08-01T10:00:00+07:00"),
                OffsetDateTime.parse("2026-08-01T12:00:00+07:00"),
                new BigDecimal("90000"),
                List.of(
                        new ShowtimeSeatResponse(1L, "A", 1, 1L, "VIP", new BigDecimal("135000.00"), SeatStatus.AVAILABLE),
                        new ShowtimeSeatResponse(2L, "A", 2, 1L, "VIP", new BigDecimal("135000.00"), SeatStatus.BOOKED)
                )
        );
        when(showtimeService.getSeatMap(eq(1L))).thenReturn(response);

        mockMvc.perform(get("/api/showtimes/1/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.movieTitle").value("Avengers"))
                .andExpect(jsonPath("$.seats[0].seatTypeName").value("VIP"))
                .andExpect(jsonPath("$.seats[0].status").value("AVAILABLE"))
                .andExpect(jsonPath("$.seats[1].status").value("BOOKED"));
    }

    @Test
    void getSeatMap_returns404WhenMissing() throws Exception {
        when(showtimeService.getSeatMap(eq(99L)))
                .thenThrow(new ResourceNotFoundException("Khong tim thay suat chieu voi id=99"));

        mockMvc.perform(get("/api/showtimes/99/seats"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Khong tim thay suat chieu voi id=99"));
    }
}
