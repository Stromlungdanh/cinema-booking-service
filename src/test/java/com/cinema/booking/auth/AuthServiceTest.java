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
import com.cinema.booking.security.GoogleTokenVerifierService;
import com.cinema.booking.security.GoogleUserInfo;
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

    @Mock
    private GoogleTokenVerifierService googleTokenVerifierService;

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

    @Test
    void loginWithGoogle_createsNewUserWhenNoneExists() {
        GoogleUserInfo info = new GoogleUserInfo("google-sub-1", "new-google@example.com", "Nguyen Van B", true);
        when(googleTokenVerifierService.verify("valid-id-token")).thenReturn(info);
        when(userRepository.findByProviderAndProviderId("GOOGLE", "google-sub-1")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new-google@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(2L);
            return user;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("fake-jwt");

        AuthResponse response = authService.loginWithGoogle(new GoogleLoginRequest("valid-id-token"));

        assertEquals("fake-jwt", response.token());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("GOOGLE", captor.getValue().getProvider());
        assertEquals("google-sub-1", captor.getValue().getProviderId());
        assertEquals("new-google@example.com", captor.getValue().getEmail());
    }

    @Test
    void loginWithGoogle_findsExistingUserByProviderAndProviderId() {
        User user = new User();
        user.setId(3L);
        user.setEmail("returning@example.com");
        user.setProvider("GOOGLE");
        user.setProviderId("google-sub-2");
        GoogleUserInfo info = new GoogleUserInfo("google-sub-2", "returning@example.com", "Nguyen Van C", true);
        when(googleTokenVerifierService.verify("valid-id-token")).thenReturn(info);
        when(userRepository.findByProviderAndProviderId("GOOGLE", "google-sub-2")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("fake-jwt");

        AuthResponse response = authService.loginWithGoogle(new GoogleLoginRequest("valid-id-token"));

        assertEquals(3L, response.userId());
    }

    @Test
    void loginWithGoogle_autoLinksExistingLocalAccountByEmail() {
        User localUser = new User();
        localUser.setId(4L);
        localUser.setEmail("local@example.com");
        localUser.setPasswordHash("hashed-password");
        localUser.setProvider("LOCAL");
        GoogleUserInfo info = new GoogleUserInfo("google-sub-3", "local@example.com", "Nguyen Van D", true);
        when(googleTokenVerifierService.verify("valid-id-token")).thenReturn(info);
        when(userRepository.findByProviderAndProviderId("GOOGLE", "google-sub-3")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("local@example.com")).thenReturn(Optional.of(localUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateToken(any(User.class))).thenReturn("fake-jwt");

        authService.loginWithGoogle(new GoogleLoginRequest("valid-id-token"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("GOOGLE", captor.getValue().getProvider());
        assertEquals("google-sub-3", captor.getValue().getProviderId());
        assertEquals("hashed-password", captor.getValue().getPasswordHash());
    }

    @Test
    void loginWithGoogle_throwsWhenEmailNotVerified() {
        GoogleUserInfo info = new GoogleUserInfo("google-sub-4", "unverified@example.com", "Nguyen Van E", false);
        when(googleTokenVerifierService.verify("valid-id-token")).thenReturn(info);

        GoogleLoginRequest request = new GoogleLoginRequest("valid-id-token");

        assertThrows(InvalidCredentialsException.class, () -> authService.loginWithGoogle(request));
    }

    @Test
    void loginWithGoogle_throwsWhenAccountIsLocked() {
        User lockedUser = new User();
        lockedUser.setId(5L);
        lockedUser.setEmail("locked-google@example.com");
        lockedUser.setActive(false);
        GoogleUserInfo info = new GoogleUserInfo("google-sub-5", "locked-google@example.com", "Nguyen Van F", true);
        when(googleTokenVerifierService.verify("valid-id-token")).thenReturn(info);
        when(userRepository.findByProviderAndProviderId("GOOGLE", "google-sub-5")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("locked-google@example.com")).thenReturn(Optional.of(lockedUser));

        GoogleLoginRequest request = new GoogleLoginRequest("valid-id-token");

        assertThrows(InvalidCredentialsException.class, () -> authService.loginWithGoogle(request));
    }
}
