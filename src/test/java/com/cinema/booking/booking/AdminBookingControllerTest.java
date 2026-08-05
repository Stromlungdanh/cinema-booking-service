package com.cinema.booking.booking;

import com.cinema.booking.booking.dto.BookingResponse;
import com.cinema.booking.booking.dto.BookingSeatResponse;
import com.cinema.booking.common.exception.BookingConflictException;
import com.cinema.booking.common.exception.ResourceNotFoundException;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminBookingController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminBookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingService bookingService;

    private BookingResponse sampleResponse(Long id, Long userId, BookingStatus status) {
        return new BookingResponse(
                id, userId, 1L, "Avengers", "Phong 1",
                OffsetDateTime.parse("2026-08-01T10:00:00+07:00"),
                status,
                new BigDecimal("90000.00"),
                OffsetDateTime.parse("2026-08-01T09:00:00+07:00"),
                null,
                List.of(new BookingSeatResponse(1L, "A", 1, new BigDecimal("90000.00")))
        );
    }

    @Test
    void findAll_returns200WithFilteredList() throws Exception {
        when(bookingService.findAllForAdmin(eq(BookingStatus.PENDING), isNull(), isNull()))
                .thenReturn(List.of(sampleResponse(1L, 2L, BookingStatus.PENDING)));

        mockMvc.perform(get("/api/admin/bookings").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userId").value(2));
    }

    @Test
    void findById_returns200RegardlessOfOwner() throws Exception {
        when(bookingService.findByIdForAdmin(1L)).thenReturn(sampleResponse(1L, 2L, BookingStatus.PENDING));

        mockMvc.perform(get("/api/admin/bookings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(2));
    }

    @Test
    void findById_returns404WhenMissing() throws Exception {
        when(bookingService.findByIdForAdmin(99L))
                .thenThrow(new ResourceNotFoundException("Khong tim thay booking voi id=99"));

        mockMvc.perform(get("/api/admin/bookings/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancel_returns200() throws Exception {
        when(bookingService.cancelForAdmin(1L)).thenReturn(sampleResponse(1L, 2L, BookingStatus.CANCELLED));

        mockMvc.perform(patch("/api/admin/bookings/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancel_returns409WhenNotPending() throws Exception {
        when(bookingService.cancelForAdmin(1L))
                .thenThrow(new BookingConflictException("Chi huy duoc booking dang o trang thai PENDING"));

        mockMvc.perform(patch("/api/admin/bookings/1/cancel"))
                .andExpect(status().isConflict());
    }
}
