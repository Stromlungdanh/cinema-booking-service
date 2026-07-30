package com.cinema.booking.movie.dto;

import com.cinema.booking.movie.MovieStatus;

import java.time.LocalDate;

public record MovieResponse(
        Long id,
        String title,
        String description,
        Integer durationMin,
        String language,
        LocalDate releaseDate,
        String posterUrl,
        String trailerUrl,
        MovieStatus status,
        Long viewCount
) {
}
