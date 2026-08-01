package com.cinema.booking.showtime;

import com.cinema.booking.showtime.dto.ShowtimeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

// API cong khai: suat chieu tai 1 rap theo ngay (man hinh "Lich chieu cua
// rap" - tab "Chon rap"; va buoc "Chon suat" - tab "Chon phim" khi da xac
// dinh duoc rap qua hang -> ngay -> rap).
@RestController
@RequestMapping("/api/cinemas/{cinemaId}/showtimes")
@RequiredArgsConstructor
public class ShowtimePublicController {

    private final ShowtimeService showtimeService;

    @GetMapping
    public List<ShowtimeResponse> findByCinemaAndDate(
            @PathVariable Long cinemaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long movieId) {
        return showtimeService.findByCinemaAndDate(cinemaId, date, movieId);
    }
}
