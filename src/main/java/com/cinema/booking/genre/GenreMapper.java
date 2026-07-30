package com.cinema.booking.genre;

import com.cinema.booking.genre.dto.GenreRequest;
import com.cinema.booking.genre.dto.GenreResponse;

public final class GenreMapper {

    private GenreMapper() {
    }

    public static Genre toEntity(GenreRequest request) {
        Genre genre = new Genre();
        applyRequest(genre, request);
        return genre;
    }

    public static void applyRequest(Genre genre, GenreRequest request) {
        genre.setName(request.name());
    }

    public static GenreResponse toResponse(Genre genre) {
        return new GenreResponse(genre.getId(), genre.getName());
    }
}
