package com.cinema.booking.auth;

import com.cinema.booking.auth.dto.AuthResponse;
import com.cinema.booking.auth.dto.ForgotPasswordRequest;
import com.cinema.booking.auth.dto.LoginRequest;
import com.cinema.booking.auth.dto.RegisterRequest;
import com.cinema.booking.auth.dto.ResetPasswordRequest;
import com.cinema.booking.common.exception.EmailAlreadyExistsException;
import com.cinema.booking.common.exception.InvalidCredentialsException;
import com.cinema.booking.common.exception.InvalidResetTokenException;
import com.cinema.booking.security.JwtService;
import com.cinema.booking.user.User;
import com.cinema.booking.user.UserRepository;
import com.cinema.booking.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_hashesPasswordAndSavesUser() {
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("fake-jwt");

        AuthResponse response = authService.register(new RegisterRequest("Nguyen Van A", "new@example.com", "Password123!"));

        assertEquals("fake-jwt", response.token());
        assertEquals(1L, response.userId());
        assertEquals(UserRole.USER, response.role());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("hashed-password", captor.getValue().getPasswordHash());
        assertEquals(UserRole.USER, captor.getValue().getRole());
    }

    @Test
    void register_throwsWhenEmailAlreadyExists() {
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        RegisterRequest request = new RegisterRequest("Nguyen Van A", "taken@example.com", "Password123!");

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));
    }

    @Test
    void login_returnsTokenWhenPasswordMatches() {
        User user = new User();
        user.setId(1L);
        user.setName("Nguyen Van A");
        user.setEmail("user@example.com");
        user.setPasswordHash("hashed-password");
        user.setRole(UserRole.USER);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "hashed-password")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("fake-jwt");

        AuthResponse response = authService.login(new LoginRequest("user@example.com", "Password123!"));

        assertEquals("fake-jwt", response.token());
        assertEquals(1L, response.userId());
    }

    @Test
    void login_throwsWhenUserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("missing@example.com", "Password123!");

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void login_throwsWhenAccountIsLocked() {
        User user = new User();
        user.setId(1L);
        user.setEmail("locked@example.com");
        user.setPasswordHash("hashed-password");
        user.setActive(false);
        when(userRepository.findByEmail("locked@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "hashed-password")).thenReturn(true);

        LoginRequest request = new LoginRequest("locked@example.com", "Password123!");

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void login_throwsWhenPasswordDoesNotMatch() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPasswordHash("hashed-password");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

        LoginRequest request = new LoginRequest("user@example.com", "wrong-password");

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void forgotPassword_savesTokenWhenEmailExists() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        authService.forgotPassword(new ForgotPasswordRequest("user@example.com"));

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(captor.capture());
        assertEquals(user, captor.getValue().getUser());
        assertNotNull(captor.getValue().getToken());
    }

    @Test
    void forgotPassword_doesNothingWhenEmailNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        authService.forgotPassword(new ForgotPasswordRequest("missing@example.com"));

        verify(passwordResetTokenRepository, never()).save(any());
    }

    @Test
    void resetPassword_updatesPasswordWhenTokenValid() {
        User user = new User();
        user.setId(1L);
        user.setPasswordHash("old-hash");
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken("valid-token");
        resetToken.setExpiresAt(OffsetDateTime.now().plusMinutes(10));
        when(passwordResetTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("NewPassword123!")).thenReturn("new-hash");

        authService.resetPassword(new ResetPasswordRequest("valid-token", "NewPassword123!"));

        assertEquals("new-hash", user.getPasswordHash());
        assertNotNull(resetToken.getUsedAt());
    }

    @Test
    void resetPassword_throwsWhenTokenNotFound() {
        when(passwordResetTokenRepository.findByToken("missing-token")).thenReturn(Optional.empty());

        ResetPasswordRequest request = new ResetPasswordRequest("missing-token", "NewPassword123!");

        assertThrows(InvalidResetTokenException.class, () -> authService.resetPassword(request));
    }

    @Test
    void resetPassword_throwsWhenTokenExpired() {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(new User());
        resetToken.setToken("expired-token");
        resetToken.setExpiresAt(OffsetDateTime.now().minusMinutes(1));
        when(passwordResetTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(resetToken));

        ResetPasswordRequest request = new ResetPasswordRequest("expired-token", "NewPassword123!");

        assertThrows(InvalidResetTokenException.class, () -> authService.resetPassword(request));
    }

    @Test
    void resetPassword_throwsWhenTokenAlreadyUsed() {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(new User());
        resetToken.setToken("used-token");
        resetToken.setExpiresAt(OffsetDateTime.now().plusMinutes(10));
        resetToken.setUsedAt(OffsetDateTime.now().minusMinutes(1));
        when(passwordResetTokenRepository.findByToken("used-token")).thenReturn(Optional.of(resetToken));

        ResetPasswordRequest request = new ResetPasswordRequest("used-token", "NewPassword123!");

        assertThrows(InvalidResetTokenException.class, () -> authService.resetPassword(request));
    }
}
