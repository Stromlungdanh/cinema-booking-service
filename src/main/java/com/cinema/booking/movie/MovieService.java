package com.cinema.booking.movie;

import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.movie.dto.MovieRequest;
import com.cinema.booking.movie.dto.MovieResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    @Transactional
    public MovieResponse create(MovieRequest request) {
        Movie movie = MovieMapper.toEntity(request);
        return MovieMapper.toResponse(movieRepository.save(movie));
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
}
