package com.cinema.booking.brand;

import com.cinema.booking.brand.dto.BrandRequest;
import com.cinema.booking.brand.dto.BrandResponse;
import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BrandController.class)
@AutoConfigureMockMvc(addFilters = false)
class BrandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BrandService brandService;

    private BrandRequest validRequest() {
        return new BrandRequest("BHD Star", "http://logo.jpg");
    }

    private BrandResponse sampleResponse(Long id) {
        return new BrandResponse(id, "BHD Star", "http://logo.jpg");
    }

    @Test
    void create_returns201WithBody() throws Exception {
        when(brandService.create(any())).thenReturn(sampleResponse(1L));

        mockMvc.perform(post("/api/admin/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("BHD Star"));
    }

    @Test
    void create_returns400WhenNameBlank() throws Exception {
        BrandRequest invalid = new BrandRequest("", null);

        mockMvc.perform(post("/api/admin/brands")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void findById_returns404WhenMissing() throws Exception {
        when(brandService.findById(eq(99L)))
                .thenThrow(new ResourceNotFoundException("Khong tim thay hang voi id=99"));

        mockMvc.perform(get("/api/admin/brands/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Khong tim thay hang voi id=99"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/admin/brands/1"))
                .andExpect(status().isNoContent());
    }
}
