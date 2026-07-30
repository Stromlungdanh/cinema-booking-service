package com.cinema.booking.movie;

import com.cinema.booking.actor.Actor;
import com.cinema.booking.actor.ActorRepository;
import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.genre.Genre;
import com.cinema.booking.genre.GenreRepository;
import com.cinema.booking.movie.dto.MovieCastRequest;
import com.cinema.booking.movie.dto.MovieRequest;
import com.cinema.booking.movie.dto.MovieResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final ActorRepository actorRepository;

    @Transactional
    public MovieResponse create(MovieRequest request) {
        Movie movie = MovieMapper.toEntity(request);
        // Luu truoc de Movie co id (IDENTITY chi sinh id sau khi insert) - can id
        // nay de dung lam khoa chinh ghep cua MovieCast ben duoi.
        movie = movieRepository.save(movie);
        movie.getGenres().addAll(resolveGenres(request.genreIds()));
        movie.getCast().addAll(resolveCast(movie, request.cast()));
        return MovieMapper.toResponse(movie);
    }

    @Transactional(readOnly = true)
    public List<MovieResponse> findAll() {
        return movieRepository.findAll().stream()
                .map(MovieMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MovieResponse findById(Long id) {
        return MovieMapper.toResponse(getMovieOrThrow(id));
    }

    @Transactional
    public MovieResponse update(Long id, MovieRequest request) {
        Movie movie = getMovieOrThrow(id);
        MovieMapper.applyRequest(movie, request);
        // Khong goi movieRepository.save(movie): trong @Transactional, entity lay ra
        // tu findById dang o trang thai "managed" - Hibernate tu dong UPDATE khi
        // transaction commit (dirty checking), goi save() them la thua.
        // Genres/cast la replace-all: clear() + addAll() de cascade/orphanRemoval
        // tu xoa quan he cu, them quan he moi.
        movie.getGenres().clear();
        movie.getGenres().addAll(resolveGenres(request.genreIds()));
        movie.getCast().clear();
        movie.getCast().addAll(resolveCast(movie, request.cast()));
        return MovieMapper.toResponse(movie);
    }

    @Transactional
    public void delete(Long id) {
        movieRepository.delete(getMovieOrThrow(id));
    }

    private Movie getMovieOrThrow(Long id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay phim voi id=" + id));
    }

    private Set<Genre> resolveGenres(List<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Genre> found = genreRepository.findAllById(genreIds);
        if (found.size() != new HashSet<>(genreIds).size()) {
            throw new ResourceNotFoundException("Khong tim thay 1 hoac nhieu the loai voi id da cho");
        }
        return new HashSet<>(found);
    }

    private List<MovieCast> resolveCast(Movie movie, List<MovieCastRequest> castRequests) {
        if (castRequests == null || castRequests.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> actorIds = castRequests.stream().map(MovieCastRequest::actorId).toList();
        Map<Long, Actor> actorsById = actorRepository.findAllById(actorIds).stream()
                .collect(Collectors.toMap(Actor::getId, Function.identity()));
        if (actorsById.size() != new HashSet<>(actorIds).size()) {
            throw new ResourceNotFoundException("Khong tim thay 1 hoac nhieu dien vien voi id da cho");
        }
        return castRequests.stream()
                .map(request -> MovieMapper.toMovieCast(movie, actorsById.get(request.actorId()), request))
                .toList();
    }
}
