package com.cinema.booking.movie.dto;

import com.cinema.booking.genre.dto.GenreResponse;
import com.cinema.booking.movie.MovieStatus;

import java.time.LocalDate;
import java.util.List;

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
        Long viewCount,
        List<GenreResponse> genres,
        List<MovieCastResponse> cast
) {
}
