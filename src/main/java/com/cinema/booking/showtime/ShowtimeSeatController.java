package com.cinema.booking.showtime;

import com.cinema.booking.showtime.dto.ShowtimeSeatMapResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// API cong khai: so do ghe cua 1 suat chieu (man hinh "Chon ghe", ca 2 tab).
@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
public class ShowtimeSeatController {

    private final ShowtimeService showtimeService;

    @GetMapping("/{id}/seats")
    public ShowtimeSeatMapResponse getSeatMap(@PathVariable Long id) {
        return showtimeService.getSeatMap(id);
    }
}
