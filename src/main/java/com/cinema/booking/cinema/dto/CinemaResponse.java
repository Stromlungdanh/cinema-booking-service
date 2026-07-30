package com.cinema.booking.cinema.dto;

public record CinemaResponse(
        Long id,
        Long brandId,
        String brandName,
        String name,
        String address,
        String city
) {
}
