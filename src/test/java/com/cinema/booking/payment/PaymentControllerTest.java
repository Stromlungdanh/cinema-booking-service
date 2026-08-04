package com.cinema.booking.payment;

import com.cinema.booking.common.exception.BookingConflictException;
import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.payment.dto.PaymentRequest;
import com.cinema.booking.payment.dto.PaymentResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    private PaymentResponse sampleResponse() {
        return new PaymentResponse(
                1L, 1L, PaymentStatus.SUCCESS, "FAKE_GATEWAY", "FAKE-abc",
                "idem-1", OffsetDateTime.parse("2026-08-04T09:00:00+07:00"),
                "CB-ABCD1234", "data:image/png;base64,abc123"
        );
    }

    @Test
    void pay_returns201WithTicketInfo() throws Exception {
        when(paymentService.pay(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PaymentRequest(1L, "idem-1"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.ticketCode").value("CB-ABCD1234"))
                .andExpect(jsonPath("$.qrCodeBase64").value("data:image/png;base64,abc123"));
    }

    @Test
    void pay_returns400WhenIdempotencyKeyBlank() throws Exception {
        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PaymentRequest(1L, ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.idempotencyKey").exists());
    }

    @Test
    void pay_returns404WhenBookingNotFound() throws Exception {
        when(paymentService.pay(any()))
                .thenThrow(new ResourceNotFoundException("Khong tim thay booking voi id=99"));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PaymentRequest(99L, "idem-1"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void pay_returns409WhenBookingNotPending() throws Exception {
        when(paymentService.pay(any()))
                .thenThrow(new BookingConflictException("Chi thanh toan duoc booking dang o trang thai PENDING"));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PaymentRequest(1L, "idem-1"))))
                .andExpect(status().isConflict());
    }
}
