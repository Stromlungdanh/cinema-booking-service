package com.cinema.booking.showtime;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    // Man hinh public: suat chieu tai 1 rap theo ngay (ca 2 tab "Chon phim"/"Chon rap").
    List<Showtime> findByRoom_Cinema_IdAndStartTimeBetween(Long cinemaId, OffsetDateTime from, OffsetDateTime to);

    List<Showtime> findByRoom_Cinema_IdAndMovie_IdAndStartTimeBetween(
            Long cinemaId, Long movieId, OffsetDateTime from, OffsetDateTime to);
}
