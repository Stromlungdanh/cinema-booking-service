package com.cinema.booking.genre;

import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.genre.dto.GenreRequest;
import com.cinema.booking.genre.dto.GenreResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;

    @Transactional
    public GenreResponse create(GenreRequest request) {
        Genre genre = GenreMapper.toEntity(request);
        return GenreMapper.toResponse(genreRepository.save(genre));
    }

    @Transactional(readOnly = true)
    public List<GenreResponse> findAll() {
        return genreRepository.findAll().stream()
                .map(GenreMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public GenreResponse findById(Long id) {
        return GenreMapper.toResponse(getGenreOrThrow(id));
    }

    @Transactional
    public GenreResponse update(Long id, GenreRequest request) {
        Genre genre = getGenreOrThrow(id);
        GenreMapper.applyRequest(genre, request);
        return GenreMapper.toResponse(genre);
    }

    @Transactional
    public void delete(Long id) {
        genreRepository.delete(getGenreOrThrow(id));
    }

    private Genre getGenreOrThrow(Long id) {
        return genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay the loai voi id=" + id));
    }
}
