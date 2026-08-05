package com.cinema.booking.booking;

import com.cinema.booking.booking.dto.BookingRequest;
import com.cinema.booking.booking.dto.BookingResponse;
import com.cinema.booking.booking.dto.BookingSeatResponse;
import com.cinema.booking.booking.dto.TicketResponse;
import com.cinema.booking.common.exception.BookingConflictException;
import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.security.UserPrincipal;
import com.cinema.booking.user.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
@AutoConfigureMockMvc(addFilters = false)
class BookingControllerTest {

    private static final Long CURRENT_USER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    // addFilters=false tat JwtAuthenticationFilter nen khong ai set
    // SecurityContext ho. MockMvc.perform() chay dong bo tren cung thread
    // test nen set truc tiep SecurityContextHolder o day la du de
    // @AuthenticationPrincipal trong BookingController khong bi null -
    // .with(authentication(...)) cua spring-security-test KHONG dung duoc
    // vi no can filter chain that (dang bi tat) de ap dung.
    @BeforeEach
    void setUpAuthentication() {
        UserPrincipal principal = new UserPrincipal(CURRENT_USER_ID, "user@example.com", UserRole.USER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    private BookingRequest validRequest() {
        return new BookingRequest(1L, List.of(1L, 2L));
    }

    private BookingResponse sampleResponse(Long id, BookingStatus status) {
        return new BookingResponse(
                id, 1L, 1L, "Avengers", "Phong 1",
                OffsetDateTime.parse("2026-08-01T10:00:00+07:00"),
                status,
                new BigDecimal("225000.00"),
                OffsetDateTime.parse("2026-08-01T09:00:00+07:00"),
                status == BookingStatus.PAID ? "CB-ABCD1234" : null,
                List.of(new BookingSeatResponse(1L, "A", 1, new BigDecimal("90000.00")),
                        new BookingSeatResponse(2L, "A", 2, new BigDecimal("135000.00")))
        );
    }

    @Test
    void create_returns201WithBody() throws Exception {
        when(bookingService.create(eq(CURRENT_USER_ID), any())).thenReturn(sampleResponse(1L, BookingStatus.PENDING));

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
        BookingRequest invalid = new BookingRequest(1L, List.of());

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.seatIds").exists());
    }

    @Test
    void create_returns409WhenSeatAlreadyBooked() throws Exception {
        when(bookingService.create(eq(CURRENT_USER_ID), any()))
                .thenThrow(new BookingConflictException("Ghe da duoc dat: [1]"));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Ghe da duoc dat: [1]"));
    }

    @Test
    void findById_returns404WhenMissing() throws Exception {
        when(bookingService.findById(eq(99L), eq(CURRENT_USER_ID)))
                .thenThrow(new ResourceNotFoundException("Khong tim thay booking voi id=99"));

        mockMvc.perform(get("/api/bookings/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Khong tim thay booking voi id=99"));
    }

    @Test
    void findByCurrentUser_returns200WithList() throws Exception {
        when(bookingService.findByUser(CURRENT_USER_ID)).thenReturn(List.of(sampleResponse(1L, BookingStatus.PENDING)));

        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void cancel_returns200WithUpdatedStatus() throws Exception {
        when(bookingService.cancel(1L, CURRENT_USER_ID)).thenReturn(sampleResponse(1L, BookingStatus.CANCELLED));

        mockMvc.perform(patch("/api/bookings/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancel_returns409WhenNotPending() throws Exception {
        when(bookingService.cancel(1L, CURRENT_USER_ID))
                .thenThrow(new BookingConflictException("Chi huy duoc booking dang o trang thai PENDING"));

        mockMvc.perform(patch("/api/bookings/1/cancel"))
                .andExpect(status().isConflict());
    }

    @Test
    void getTicket_returns200WithQrCode() throws Exception {
        TicketResponse ticket = new TicketResponse(
                1L, "CB-ABCD1234", "data:image/png;base64,abc123",
                "Avengers", "Phong 1",
                OffsetDateTime.parse("2026-08-01T10:00:00+07:00"),
                List.of(new BookingSeatResponse(1L, "A", 1, new BigDecimal("90000.00")))
        );
        when(bookingService.getTicket(1L, CURRENT_USER_ID)).thenReturn(ticket);

        mockMvc.perform(get("/api/bookings/1/ticket"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketCode").value("CB-ABCD1234"))
                .andExpect(jsonPath("$.qrCodeBase64").value("data:image/png;base64,abc123"));
    }

    @Test
    void getTicket_returns409WhenNotPaid() throws Exception {
        when(bookingService.getTicket(1L, CURRENT_USER_ID))
                .thenThrow(new BookingConflictException("Booking chua thanh toan, chua co ve"));

        mockMvc.perform(get("/api/bookings/1/ticket"))
                .andExpect(status().isConflict());
    }
}
