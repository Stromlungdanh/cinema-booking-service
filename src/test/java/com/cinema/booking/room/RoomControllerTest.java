package com.cinema.booking.room;

import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.room.dto.RoomRequest;
import com.cinema.booking.room.dto.RoomResponse;
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

@WebMvcTest(RoomController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoomService roomService;

    private RoomRequest validRequest() {
        return new RoomRequest(1L, "Phong 1", "IMAX");
    }

    private RoomResponse sampleResponse(Long id) {
        return new RoomResponse(id, 1L, "BHD Star Bitexco", "Phong 1", "IMAX");
    }

    @Test
    void create_returns201WithBody() throws Exception {
        when(roomService.create(any())).thenReturn(sampleResponse(1L));

        mockMvc.perform(post("/api/admin/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.roomType").value("IMAX"));
    }

    @Test
    void create_returns400WhenNameBlank() throws Exception {
        RoomRequest invalid = new RoomRequest(1L, "", "2D");

        mockMvc.perform(post("/api/admin/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void findById_returns404WhenMissing() throws Exception {
        when(roomService.findById(eq(99L)))
                .thenThrow(new ResourceNotFoundException("Khong tim thay phong voi id=99"));

        mockMvc.perform(get("/api/admin/rooms/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Khong tim thay phong voi id=99"));
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/api/admin/rooms/1"))
                .andExpect(status().isNoContent());
    }
}
