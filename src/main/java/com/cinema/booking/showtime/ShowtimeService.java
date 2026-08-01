package com.cinema.booking.showtime;

import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.movie.Movie;
import com.cinema.booking.movie.MovieRepository;
import com.cinema.booking.room.Room;
import com.cinema.booking.room.RoomRepository;
import com.cinema.booking.showtime.dto.ShowtimeRequest;
import com.cinema.booking.showtime.dto.ShowtimeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;

    @Transactional
    public ShowtimeResponse create(ShowtimeRequest request) {
        Movie movie = getMovieOrThrow(request.movieId());
        Room room = getRoomOrThrow(request.roomId());
        Showtime showtime = ShowtimeMapper.toEntity(movie, room, request);
        return ShowtimeMapper.toResponse(showtimeRepository.save(showtime));
    }

    @Transactional(readOnly = true)
    public List<ShowtimeResponse> findAll() {
        return showtimeRepository.findAll().stream()
                .map(ShowtimeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ShowtimeResponse findById(Long id) {
        return ShowtimeMapper.toResponse(getShowtimeOrThrow(id));
    }

    @Transactional
    public ShowtimeResponse update(Long id, ShowtimeRequest request) {
        Showtime showtime = getShowtimeOrThrow(id);
        Movie movie = getMovieOrThrow(request.movieId());
        Room room = getRoomOrThrow(request.roomId());
        ShowtimeMapper.applyRequest(showtime, movie, room, request);
        return ShowtimeMapper.toResponse(showtime);
    }

    @Transactional
    public void delete(Long id) {
        showtimeRepository.delete(getShowtimeOrThrow(id));
    }

    private Showtime getShowtimeOrThrow(Long id) {
        return showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay suat chieu voi id=" + id));
    }

    private Movie getMovieOrThrow(Long movieId) {
        return movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay phim voi id=" + movieId));
    }

    private Room getRoomOrThrow(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay phong voi id=" + roomId));
    }
}
