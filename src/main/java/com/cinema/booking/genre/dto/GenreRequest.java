package com.cinema.booking.genre.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenreRequest(
        @NotBlank(message = "Ten the loai khong duoc de trong")
        @Size(max = 50, message = "Ten the loai toi da 50 ky tu") String name
) {
}
