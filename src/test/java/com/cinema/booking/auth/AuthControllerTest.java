package com.cinema.booking.auth;

import com.cinema.booking.auth.dto.AuthResponse;
import com.cinema.booking.auth.dto.ForgotPasswordRequest;
import com.cinema.booking.auth.dto.GoogleLoginRequest;
import com.cinema.booking.auth.dto.LoginRequest;
import com.cinema.booking.auth.dto.RegisterRequest;
import com.cinema.booking.auth.dto.ResetPasswordRequest;
import com.cinema.booking.common.exception.EmailAlreadyExistsException;
import com.cinema.booking.common.exception.InvalidCredentialsException;
import com.cinema.booking.common.exception.InvalidResetTokenException;
import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.security.UserPrincipal;
import com.cinema.booking.user.UserRole;
import com.cinema.booking.user.UserService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    private static final Long CURRENT_USER_ID = 1L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserService userService;

    // addFilters=false tat JwtAuthenticationFilter - set truc tiep
    // SecurityContext o day de @AuthenticationPrincipal trong
    // AuthController.me khong bi null, giong pattern BookingControllerTest.
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

    private AuthResponse sampleResponse() {
        return new AuthResponse("fake-jwt", 1L, "Nguyen Van A", "user@example.com", UserRole.USER);
    }

    @Test
    void register_returns201WithToken() throws Exception {
        when(authService.register(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("Nguyen Van A", "user@example.com", "Password123!"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("fake-jwt"))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void register_returns400WhenPasswordTooShort() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("Nguyen Van A", "user@example.com", "short"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void register_returns409WhenEmailAlreadyExists() throws Exception {
        when(authService.register(any()))
                .thenThrow(new EmailAlreadyExistsException("Email da duoc dang ky: user@example.com"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new RegisterRequest("Nguyen Van A", "user@example.com", "Password123!"))))
                .andExpect(status().isConflict());
    }

    @Test
    void login_returns200WithToken() throws Exception {
        when(authService.login(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("user@example.com", "Password123!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt"));
    }

    @Test
    void login_returns401WhenInvalidCredentials() throws Exception {
        when(authService.login(any()))
                .thenThrow(new InvalidCredentialsException("Email hoac mat khau khong dung"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("user@example.com", "wrong-password"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void forgotPassword_returns200() throws Exception {
        doNothing().when(authService).forgotPassword(any());

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ForgotPasswordRequest("user@example.com"))))
                .andExpect(status().isOk());
    }

    @Test
    void forgotPassword_returns400WhenEmailInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ForgotPasswordRequest("not-an-email"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_returns200() throws Exception {
        doNothing().when(authService).resetPassword(any());

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ResetPasswordRequest("valid-token", "NewPassword123!"))))
                .andExpect(status().isOk());
    }

    @Test
    void resetPassword_returns400WhenTokenInvalid() throws Exception {
        doThrow(new InvalidResetTokenException("Token khong hop le"))
                .when(authService).resetPassword(any());

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ResetPasswordRequest("bad-token", "NewPassword123!"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginWithGoogle_returns200WithToken() throws Exception {
        when(authService.loginWithGoogle(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GoogleLoginRequest("valid-id-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt"));
    }

    @Test
    void loginWithGoogle_returns401WhenTokenInvalid() throws Exception {
        when(authService.loginWithGoogle(any()))
                .thenThrow(new InvalidCredentialsException("Google token khong hop le"));

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GoogleLoginRequest("bad-token"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginWithGoogle_returns400WhenIdTokenBlank() throws Exception {
        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GoogleLoginRequest(""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void me_returns200WithCurrentUser() throws Exception {
        UserResponse response = new UserResponse(
                CURRENT_USER_ID, "Nguyen Van A", "user@example.com", "LOCAL", UserRole.USER, true,
                OffsetDateTime.parse("2026-08-01T10:00:00+07:00"));
        when(userService.findById(CURRENT_USER_ID)).thenReturn(response);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void me_returns404WhenUserMissing() throws Exception {
        when(userService.findById(CURRENT_USER_ID))
                .thenThrow(new ResourceNotFoundException("Khong tim thay user voi id=" + CURRENT_USER_ID));

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isNotFound());
    }
}
