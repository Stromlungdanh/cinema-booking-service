package com.cinema.booking.security;

import com.cinema.booking.auth.AuthController;
import com.cinema.booking.auth.AuthService;
import com.cinema.booking.brand.BrandController;
import com.cinema.booking.brand.BrandService;
import com.cinema.booking.movie.MoviePublicController;
import com.cinema.booking.movie.MovieService;
import com.cinema.booking.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Test slice XAC NHAN rule phan quyen THAT cua SecurityConfig (khac voi 16
// file *ControllerTest khac dang tat security bang addFilters=false). Import
// thang SecurityConfig de dung dung filter chain that, khong phai chain mac
// dinh cua Spring Boot khi chua co bean SecurityFilterChain nao.
@WebMvcTest({BrandController.class, MoviePublicController.class, AuthController.class})
@Import(SecurityConfig.class)
class SecurityConfigAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BrandService brandService;

    @MockBean
    private MovieService movieService;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    // SecurityConfig can bean nay de tao JwtAuthenticationFilter (constructor
    // injection), khong dung toi trong cac test nay vi @WithMockUser tu set
    // SecurityContext truoc khi filter chain chay, va khong request nao gui
    // header Authorization.
    @MockBean
    private JwtService jwtService;

    @Test
    void adminEndpoint_returns401WhenAnonymous() throws Exception {
        mockMvc.perform(get("/api/admin/brands"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminEndpoint_returns403ForNonAdminRole() throws Exception {
        mockMvc.perform(get("/api/admin/brands"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminEndpoint_returns200ForAdminRole() throws Exception {
        org.mockito.Mockito.when(brandService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/brands"))
                .andExpect(status().isOk());
    }

    @Test
    void publicEndpoint_returns200WithoutAuthentication() throws Exception {
        org.mockito.Mockito.when(movieService.search(null, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isOk());
    }

    // /api/auth/me phai doi authenticated du nam duoi tien to /api/auth/**
    // (permitAll) - rule cu the "/api/auth/me" phai duoc dat truoc rule rong
    // hon trong SecurityConfig, test nay xac nhan dung thu tu do.
    @Test
    void authMeEndpoint_returns401WhenAnonymous() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
