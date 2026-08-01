package com.cinema.booking.cinema;

import com.cinema.booking.cinema.dto.CinemaResponse;
import com.cinema.booking.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CinemaPublicController.class)
class CinemaPublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CinemaService cinemaService;

    @Test
    void findByBrand_returns200WithBody() throws Exception {
        when(cinemaService.findByBrand(eq(1L)))
                .thenReturn(List.of(new CinemaResponse(1L, 1L, "BHD Star", "BHD Star Bitexco", "Quan 1", "HCM")));

        mockMvc.perform(get("/api/brands/1/cinemas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("BHD Star Bitexco"));
    }

    @Test
    void findByBrand_returns404WhenBrandMissing() throws Exception {
        when(cinemaService.findByBrand(eq(99L)))
                .thenThrow(new ResourceNotFoundException("Khong tim thay hang voi id=99"));

        mockMvc.perform(get("/api/brands/99/cinemas"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Khong tim thay hang voi id=99"));
    }
}
