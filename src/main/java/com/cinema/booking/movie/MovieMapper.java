package com.cinema.booking.movie;

import com.cinema.booking.actor.Actor;
import com.cinema.booking.genre.dto.GenreResponse;
import com.cinema.booking.movie.dto.MovieCastRequest;
import com.cinema.booking.movie.dto.MovieCastResponse;
import com.cinema.booking.movie.dto.MovieRequest;
import com.cinema.booking.movie.dto.MovieResponse;

// Mapper thu cong (khong dung MapStruct) de thay ro moi truong hop dang lam gi.
// Khi so field nhieu len o cac entity sau (Cinema, Room...) co the can nhac MapStruct.
// Mapper khong tu query DB - Service resolve Genre/Actor entity truoc, mapper chi lap rap.
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
                movie.getViewCount(),
                movie.getGenres().stream()
                        .map(genre -> new GenreResponse(genre.getId(), genre.getName()))
                        .toList(),
                movie.getCast().stream()
                        .map(MovieMapper::toCastResponse)
                        .toList()
        );
    }

    public static MovieCast toMovieCast(Movie movie, Actor actor, MovieCastRequest request) {
        MovieCast movieCast = new MovieCast();
        movieCast.setId(new MovieCastId(movie.getId(), actor.getId()));
        movieCast.setMovie(movie);
        movieCast.setActor(actor);
        movieCast.setRoleName(request.roleName());
        return movieCast;
    }

    private static MovieCastResponse toCastResponse(MovieCast movieCast) {
        Actor actor = movieCast.getActor();
        return new MovieCastResponse(
                actor.getId(),
                actor.getName(),
                actor.getAvatarUrl(),
                movieCast.getRoleName()
        );
    }
}
