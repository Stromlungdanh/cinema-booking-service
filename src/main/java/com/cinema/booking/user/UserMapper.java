package com.cinema.booking.user;

import com.cinema.booking.user.dto.UserResponse;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getProvider(),
                user.getRole(),
                user.getActive(),
                user.getCreatedAt()
        );
    }
}
