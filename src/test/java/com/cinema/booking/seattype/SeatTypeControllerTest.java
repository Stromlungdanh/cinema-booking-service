package com.cinema.booking.seattype;

import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.seattype.dto.SeatTypeRequest;
import com.cinema.booking.seattype.dto.SeatTypeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SeatTypeController.class)
class SeatTypeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SeatTypeService seatTypeService;

    private SeatTypeRequest validRequest() {
        return new SeatTypeRequest("VIP", new BigDecimal("1.50"));
    }

    private SeatTypeResponse sampleResponse(Long id) {
        return new SeatTypeResponse(id, "VIP", new BigDecimal("1.50"));
    }

    @Test
    void create_returns201WithBody() throws Exception {
        when(seatTypeService.create(any())).thenReturn(sampleResponse(1L));

        mockMvc.perform(post("/api/admin/seat-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("VIP"));
    }

    @Test
    void create_returns400WhenNameBlank() throws Exception {
        SeatTypeRequest invalid = new SeatTypeRequest("", new BigDecimal("1.50"));

        mockMvc.perform(post("/api/admin/seat-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void findById_returns404WhenMissing() throws Exception {
        when(seatTypeService.findById(eq(99L)))
                .thenThrow(new ResourceNotFoundException("Khong tim thay loai ghe voi id=99"));

        mockMvc.perform(get("/api/admin/seat-types/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Khong tim thay loai ghe voi id=99"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/admin/seat-types/1"))
                .andExpect(status().isNoContent());
    }
}
