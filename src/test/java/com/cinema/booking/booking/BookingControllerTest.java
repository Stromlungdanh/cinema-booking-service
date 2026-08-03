package com.cinema.booking.booking;

import com.cinema.booking.booking.dto.BookingRequest;
import com.cinema.booking.booking.dto.BookingResponse;
import com.cinema.booking.booking.dto.BookingSeatResponse;
import com.cinema.booking.common.exception.BookingConflictException;
import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingRequest validRequest() {
        return new BookingRequest(1L, 1L, List.of(1L, 2L));
    }

    private BookingResponse sampleResponse(Long id, BookingStatus status) {
        return new BookingResponse(
                id, 1L, 1L, "Avengers", "Phong 1",
                OffsetDateTime.parse("2026-08-01T10:00:00+07:00"),
                status,
                new BigDecimal("225000.00"),
                OffsetDateTime.parse("2026-08-01T09:00:00+07:00"),
                List.of(new BookingSeatResponse(1L, "A", 1, new BigDecimal("90000.00")),
                        new BookingSeatResponse(2L, "A", 2, new BigDecimal("135000.00")))
        );
    }

    @Test
    void create_returns201WithBody() throws Exception {
        when(bookingService.create(any())).thenReturn(sampleResponse(1L, BookingStatus.PENDING));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.seats.length()").value(2));
    }

    @Test
    void create_returns400WhenSeatIdsEmpty() throws Exception {
        BookingRequest invalid = new BookingRequest(1L, 1L, List.of());

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.seatIds").exists());
    }

    @Test
    void create_returns409WhenSeatAlreadyBooked() throws Exception {
        when(bookingService.create(any()))
                .thenThrow(new BookingConflictException("Ghe da duoc dat: [1]"));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ghe da duoc dat: [1]"));
    }

    @Test
    void findById_returns404WhenMissing() throws Exception {
        when(bookingService.findById(eq(99L)))
                .thenThrow(new ResourceNotFoundException("Khong tim thay booking voi id=99"));

        mockMvc.perform(get("/api/bookings/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Khong tim thay booking voi id=99"));
    }

    @Test
    void findByUser_returns200WithList() throws Exception {
        when(bookingService.findByUser(1L)).thenReturn(List.of(sampleResponse(1L, BookingStatus.PENDING)));

        mockMvc.perform(get("/api/bookings").param("userId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void cancel_returns200WithUpdatedStatus() throws Exception {
        when(bookingService.cancel(1L)).thenReturn(sampleResponse(1L, BookingStatus.CANCELLED));

        mockMvc.perform(patch("/api/bookings/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancel_returns409WhenNotPending() throws Exception {
        when(bookingService.cancel(1L))
                .thenThrow(new BookingConflictException("Chi huy duoc booking dang o trang thai PENDING"));

        mockMvc.perform(patch("/api/bookings/1/cancel"))
                .andExpect(status().isConflict());
    }
}
