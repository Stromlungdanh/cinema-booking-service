
package com.cinema.booking.movie.dto;

import jakarta.validation.constraints.NotNull;

public record MovieCastRequest(
        @NotNull(message = "Dien vien khong duoc de trong") Long actorId,
        String roleName
) {
}
