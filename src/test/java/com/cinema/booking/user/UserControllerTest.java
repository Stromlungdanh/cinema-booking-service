package com.cinema.booking.user;

import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.common.exception.SelfActionNotAllowedException;
import com.cinema.booking.security.UserPrincipal;
import com.cinema.booking.user.dto.UpdateUserRoleRequest;
import com.cinema.booking.user.dto.UserResponse;
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

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    private static final Long CURRENT_ADMIN_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    // addFilters=false tat JwtAuthenticationFilter - set SecurityContextHolder
    // truc tiep tren thread test, dung pattern nhu BookingControllerTest.
    @BeforeEach
    void setUpAuthentication() {
        UserPrincipal principal = new UserPrincipal(CURRENT_ADMIN_ID, "admin@example.com", UserRole.ADMIN);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    private UserResponse sampleResponse(Long id, UserRole role, boolean active) {
        return new UserResponse(id, "Nguyen Van A", "user" + id + "@example.com", "LOCAL", role, active, OffsetDateTime.now());
    }

    @Test
    void findAll_returns200WithList() throws Exception {
        when(userService.findAll()).thenReturn(List.of(sampleResponse(2L, UserRole.USER, true)));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void findById_returns404WhenMissing() throws Exception {
        when(userService.findById(eq(99L)))
                .thenThrow(new ResourceNotFoundException("Khong tim thay user voi id=99"));

        mockMvc.perform(get("/api/admin/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateRole_returns200WithUpdatedRole() throws Exception {
        when(userService.updateRole(eq(2L), eq(CURRENT_ADMIN_ID), eq(UserRole.ADMIN)))
                .thenReturn(sampleResponse(2L, UserRole.ADMIN, true));

        mockMvc.perform(patch("/api/admin/users/2/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserRoleRequest(UserRole.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void updateRole_returns409WhenActingOnSelf() throws Exception {
        when(userService.updateRole(eq(CURRENT_ADMIN_ID), eq(CURRENT_ADMIN_ID), eq(UserRole.USER)))
                .thenThrow(new SelfActionNotAllowedException("Khong the tu doi role cua chinh minh"));

        mockMvc.perform(patch("/api/admin/users/" + CURRENT_ADMIN_ID + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateUserRoleRequest(UserRole.USER))))
                .andExpect(status().isConflict());
    }

    @Test
    void lock_returns200WithInactiveUser() throws Exception {
        when(userService.lock(eq(2L), eq(CURRENT_ADMIN_ID))).thenReturn(sampleResponse(2L, UserRole.USER, false));

        mockMvc.perform(patch("/api/admin/users/2/lock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void lock_returns409WhenActingOnSelf() throws Exception {
        when(userService.lock(eq(CURRENT_ADMIN_ID), eq(CURRENT_ADMIN_ID)))
                .thenThrow(new SelfActionNotAllowedException("Khong the tu khoa tai khoan cua chinh minh"));

        mockMvc.perform(patch("/api/admin/users/" + CURRENT_ADMIN_ID + "/lock"))
                .andExpect(status().isConflict());
    }

    @Test
    void unlock_returns200WithActiveUser() throws Exception {
        when(userService.unlock(2L)).thenReturn(sampleResponse(2L, UserRole.USER, true));

        mockMvc.perform(patch("/api/admin/users/2/unlock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }
}
