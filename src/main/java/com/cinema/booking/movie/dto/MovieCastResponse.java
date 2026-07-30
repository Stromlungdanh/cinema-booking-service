package com.cinema.booking.movie.dto;

public record MovieCastResponse(
        Long actorId,
        String actorName,
        String avatarUrl,
        String roleName
) {
}
