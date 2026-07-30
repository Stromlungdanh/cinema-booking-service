package com.cinema.booking.actor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActorRequest(
        @NotBlank(message = "Ten dien vien khong duoc de trong")
        @Size(max = 150, message = "Ten dien vien toi da 150 ky tu") String name,
        String avatarUrl
) {
}
