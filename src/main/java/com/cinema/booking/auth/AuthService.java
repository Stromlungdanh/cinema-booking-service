package com.cinema.booking.auth;

import com.cinema.booking.auth.dto.AuthResponse;
import com.cinema.booking.auth.dto.ForgotPasswordRequest;
import com.cinema.booking.auth.dto.LoginRequest;
import com.cinema.booking.auth.dto.RegisterRequest;
import com.cinema.booking.auth.dto.ResetPasswordRequest;
import com.cinema.booking.common.exception.EmailAlreadyExistsException;
import com.cinema.booking.common.exception.InvalidCredentialsException;
import com.cinema.booking.common.exception.InvalidResetTokenException;
import com.cinema.booking.common.util.PasswordResetTokenGenerator;
import com.cinema.booking.security.JwtService;
import com.cinema.booking.user.User;
import com.cinema.booking.user.UserRepository;
import com.cinema.booking.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final long RESET_TOKEN_TTL_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email da duoc dang ky: " + request.email());
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setProvider("LOCAL");
        user.setRole(UserRole.USER);
        user.setCreatedAt(OffsetDateTime.now());
        User saved = userRepository.save(user);

        return toAuthResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Email hoac mat khau khong dung"));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Email hoac mat khau khong dung");
        }
        if (!user.getActive()) {
            throw new InvalidCredentialsException("Tai khoan da bi khoa");
        }

        return toAuthResponse(user);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setToken(PasswordResetTokenGenerator.generate());
            resetToken.setExpiresAt(OffsetDateTime.now().plusMinutes(RESET_TOKEN_TTL_MINUTES));
            resetToken.setCreatedAt(OffsetDateTime.now());
            passwordResetTokenRepository.save(resetToken);
            log.info("Reset password token cho {}: {}", user.getEmail(), resetToken.getToken());
        });
        // Khong throw khi email khong ton tai - tranh lo email nao da dang ky (user enumeration)
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new InvalidResetTokenException("Token khong hop le"));
        if (resetToken.getUsedAt() != null || resetToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new InvalidResetTokenException("Token da het han hoac da duoc su dung");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        resetToken.setUsedAt(OffsetDateTime.now());
    }

    private AuthResponse toAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
