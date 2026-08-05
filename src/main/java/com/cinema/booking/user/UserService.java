package com.cinema.booking.user;

import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.common.exception.SelfActionNotAllowedException;
import com.cinema.booking.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserMapper.toResponse(getUserOrThrow(id));
    }

    @Transactional
    public UserResponse updateRole(Long id, Long currentAdminId, UserRole newRole) {
        requireNotSelf(id, currentAdminId, "Khong the tu doi role cua chinh minh");
        User user = getUserOrThrow(id);
        user.setRole(newRole);
        return UserMapper.toResponse(user);
    }

    @Transactional
    public UserResponse lock(Long id, Long currentAdminId) {
        requireNotSelf(id, currentAdminId, "Khong the tu khoa tai khoan cua chinh minh");
        User user = getUserOrThrow(id);
        user.setActive(false);
        return UserMapper.toResponse(user);
    }

    @Transactional
    public UserResponse unlock(Long id) {
        User user = getUserOrThrow(id);
        user.setActive(true);
        return UserMapper.toResponse(user);
    }

    private void requireNotSelf(Long targetId, Long currentAdminId, String message) {
        if (targetId.equals(currentAdminId)) {
            throw new SelfActionNotAllowedException(message);
        }
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay user voi id=" + id));
    }
}
