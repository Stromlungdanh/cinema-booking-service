package com.cinema.booking.brand.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BrandRequest(
        @NotBlank(message = "Ten hang khong duoc de trong")
        @Size(max = 150) String name,
        String logoUrl
) {
}
