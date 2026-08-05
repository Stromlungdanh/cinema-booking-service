package com.cinema.booking.user;

import com.cinema.booking.security.UserPrincipal;
import com.cinema.booking.user.dto.UpdateUserRoleRequest;
import com.cinema.booking.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public UserResponse findById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PatchMapping("/{id}/role")
    public UserResponse updateRole(@AuthenticationPrincipal UserPrincipal principal,
                                     @PathVariable Long id,
                                     @Valid @RequestBody UpdateUserRoleRequest request) {
        return userService.updateRole(id, principal.id(), request.role());
    }

    @PatchMapping("/{id}/lock")
    public UserResponse lock(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long id) {
        return userService.lock(id, principal.id());
    }

    @PatchMapping("/{id}/unlock")
    public UserResponse unlock(@PathVariable Long id) {
        return userService.unlock(id);
    }
}
