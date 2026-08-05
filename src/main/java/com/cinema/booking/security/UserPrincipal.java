package com.cinema.booking.security;

import com.cinema.booking.user.UserRole;

// Principal nhe - dung lam Authentication.getPrincipal() sau khi decode JWT.
// Khong implement UserDetails/UserDetailsService day du vi xac thuc la
// stateless, khong can load lai User tu DB moi request.
public record UserPrincipal(Long id, String email, UserRole role) {
}
