package com.cinema.booking.movie.dto;

import com.cinema.booking.movie.MovieStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

// Input cua Create/Update. Tach rieng voi MovieResponse: form nhap tu Admin
// khong nen quyet dinh duoc field nao la server-controlled (vi du viewCount).
public record MovieRequest(
        @NotBlank(message = "Ten phim khong duoc de trong") String title,
        String description,
        @NotNull(message = "Thoi luong khong duoc de trong")
        @Positive(message = "Thoi luong phai lon hon 0 phut") Integer durationMin,
        String language,
        LocalDate releaseDate,
        String posterUrl,
        String trailerUrl,
        @NotNull(message = "Trang thai khong duoc de trong") MovieStatus status
) {
}
