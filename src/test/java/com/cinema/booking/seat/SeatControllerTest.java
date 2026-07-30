package com.cinema.booking.seat;

import com.cinema.booking.seat.dto.SeatLayoutRequest;
import com.cinema.booking.seat.dto.SeatResponse;
import com.cinema.booking.seat.dto.SeatRowRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SeatController.class)
class SeatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SeatService seatService;

    private SeatLayoutRequest validRequest() {
        return new SeatLayoutRequest(List.of(new SeatRowRequest("A", 3, 10L)));
    }

    private List<SeatResponse> sampleResponse() {
        return List.of(
                new SeatResponse(1L, "A", 1, 10L, "Standard"),
                new SeatResponse(2L, "A", 2, 10L, "Standard")
        );
    }

    @Test
    void generateLayout_returns201WithSeatList() throws Exception {
        when(seatService.generateLayout(eq(1L), any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/admin/rooms/1/seats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].rowLabel").value("A"));
    }

    @Test
    void generateLayout_returns400WhenRowsEmpty() throws Exception {
        SeatLayoutRequest invalid = new SeatLayoutRequest(List.of());

        mockMvc.perform(post("/api/admin/rooms/1/seats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.rows").exists());
    }

    @Test
    void findByRoom_returnsSeatList() throws Exception {
        when(seatService.findByRoom(1L)).thenReturn(sampleResponse());

        mockMvc.perform(get("/api/admin/rooms/1/seats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
