package com.cinema.booking.user;

import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.common.exception.SelfActionNotAllowedException;
import com.cinema.booking.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user(long id, UserRole role, boolean active) {
        User user = new User();
        user.setId(id);
        user.setName("Nguyen Van A");
        user.setEmail("user" + id + "@example.com");
        user.setProvider("LOCAL");
        user.setRole(role);
        user.setActive(active);
        user.setCreatedAt(OffsetDateTime.now());
        return user;
    }

    @Test
    void findAll_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user(1L, UserRole.USER, true), user(2L, UserRole.ADMIN, true)));

        List<UserResponse> responses = userService.findAll();

        assertEquals(2, responses.size());
    }

    @Test
    void findById_throwsWhenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.findById(99L));
    }

    @Test
    void updateRole_changesRole() {
        User target = user(2L, UserRole.USER, true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        UserResponse response = userService.updateRole(2L, 1L, UserRole.ADMIN);

        assertEquals(UserRole.ADMIN, response.role());
    }

    @Test
    void updateRole_throwsWhenActingOnSelf() {
        assertThrows(SelfActionNotAllowedException.class, () -> userService.updateRole(1L, 1L, UserRole.ADMIN));
    }

    @Test
    void lock_setsActiveFalse() {
        User target = user(2L, UserRole.USER, true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        UserResponse response = userService.lock(2L, 1L);

        assertFalse(response.active());
    }

    @Test
    void lock_throwsWhenActingOnSelf() {
        assertThrows(SelfActionNotAllowedException.class, () -> userService.lock(1L, 1L));
    }

    @Test
    void unlock_setsActiveTrue() {
        User target = user(2L, UserRole.USER, false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        UserResponse response = userService.unlock(2L);

        assertTrue(response.active());
    }
}
