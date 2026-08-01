package com.cinema.booking.showtime;

import com.cinema.booking.cinema.CinemaRepository;
import com.cinema.booking.common.exception.ResourceNotFoundException;
import com.cinema.booking.movie.Movie;
import com.cinema.booking.movie.MovieRepository;
import com.cinema.booking.room.Room;
import com.cinema.booking.room.RoomRepository;
import com.cinema.booking.seat.Seat;
import com.cinema.booking.seat.SeatRepository;
import com.cinema.booking.showtime.dto.ShowtimeRequest;
import com.cinema.booking.showtime.dto.ShowtimeResponse;
import com.cinema.booking.showtime.dto.ShowtimeSeatMapResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    // Rap thuoc VN, khong co config timezone rieng - dung co dinh thay vi
    // system default zone cua server de loc theo ngay on dinh.
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final CinemaRepository cinemaRepository;
    private final SeatRepository seatRepository;

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

    // Man hinh public: suat chieu tai 1 rap theo ngay, loc them theo phim
    // (optional) - dung chung cho ca 2 tab "Chon phim"/"Chon rap".
    @Transactional(readOnly = true)
    public List<ShowtimeResponse> findByCinemaAndDate(Long cinemaId, LocalDate date, Long movieId) {
        assertCinemaExists(cinemaId);
        OffsetDateTime from = date.atStartOfDay(VN_ZONE).toOffsetDateTime();
        OffsetDateTime to = from.plusDays(1);
        List<Showtime> showtimes = movieId != null
                ? showtimeRepository.findByRoom_Cinema_IdAndMovie_IdAndStartTimeBetween(cinemaId, movieId, from, to)
                : showtimeRepository.findByRoom_Cinema_IdAndStartTimeBetween(cinemaId, from, to);
        return showtimes.stream().map(ShowtimeMapper::toResponse).toList();
    }

    // Man hinh public: so do ghe + gia cho 1 suat chieu (Chua co trang thai
    // con trong/da dat - viec do thuoc luong Booking, chua lam o buoc nay).
    @Transactional(readOnly = true)
    public ShowtimeSeatMapResponse getSeatMap(Long showtimeId) {
        Showtime showtime = getShowtimeOrThrow(showtimeId);
        List<Seat> seats = seatRepository.findByRoomIdOrderByRowLabelAscColNumberAsc(showtime.getRoom().getId());
        return ShowtimeMapper.toSeatMapResponse(showtime, seats);
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

    private void assertCinemaExists(Long cinemaId) {
        if (!cinemaRepository.existsById(cinemaId)) {
            throw new ResourceNotFoundException("Khong tim thay rap voi id=" + cinemaId);
        }
    }
}
