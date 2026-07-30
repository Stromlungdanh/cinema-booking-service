package com.cinema.booking.cinema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CinemaRequest(
        @NotNull(message = "Hang chieu phim khong duoc de trong") Long brandId,
        @NotBlank(message = "Ten rap khong duoc de trong") String name,
        String address,
        String city
) {
}
