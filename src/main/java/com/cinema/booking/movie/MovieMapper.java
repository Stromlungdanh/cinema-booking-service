package com.cinema.booking.movie;

import com.cinema.booking.movie.dto.MovieRequest;
import com.cinema.booking.movie.dto.MovieResponse;

// Mapper thu cong (khong dung MapStruct) de thay ro moi truong hop dang lam gi.
// Khi so field nhieu len o cac entity sau (Cinema, Room...) co the can nhac MapStruct.
public final class MovieMapper {

    private MovieMapper() {
    }

    public static Movie toEntity(MovieRequest request) {
        Movie movie = new Movie();
        applyRequest(movie, request);
        return movie;
    }

    public static void applyRequest(Movie movie, MovieRequest request) {
        movie.setTitle(request.title());
        movie.setDescription(request.description());
        movie.setDurationMin(request.durationMin());
        movie.setLanguage(request.language());
        movie.setReleaseDate(request.releaseDate());
        movie.setPosterUrl(request.posterUrl());
        movie.setTrailerUrl(request.trailerUrl());
        movie.setStatus(request.status());
    }

    public static MovieResponse toResponse(Movie movie) {
        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getDescription(),
                movie.getDurationMin(),
                movie.getLanguage(),
                movie.getReleaseDate(),
                movie.getPosterUrl(),
                movie.getTrailerUrl(),
                movie.getStatus(),
                movie.getViewCount()
        );
    }
}
