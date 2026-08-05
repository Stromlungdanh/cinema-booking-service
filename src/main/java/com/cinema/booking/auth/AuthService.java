package com.cinema.booking.auth;

import com.cinema.booking.auth.dto.AuthResponse;
import com.cinema.booking.auth.dto.LoginRequest;
import com.cinema.booking.auth.dto.RegisterRequest;
import com.cinema.booking.common.exception.EmailAlreadyExistsException;
import com.cinema.booking.common.exception.InvalidCredentialsException;
import com.cinema.booking.security.JwtService;
import com.cinema.booking.user.User;
import com.cinema.booking.user.UserRepository;
import com.cinema.booking.user.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
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

        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
