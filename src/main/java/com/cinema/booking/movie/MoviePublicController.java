package com.cinema.booking.movie;

import com.cinema.booking.movie.dto.MovieResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// API cong khai cho man hinh User (trang chu phim, chi tiet phim). Tach rieng
// voi /api/admin/movies - se khong yeu cau ADMIN khi them JWT sau nay.
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MoviePublicController {

    private final MovieService movieService;

    @GetMapping
    public List<MovieResponse> search(@RequestParam(required = false) MovieStatus status,
                                       @RequestParam(required = false) String q) {
        return movieService.search(status, q);
    }

    @GetMapping("/featured")
    public List<MovieResponse> featured(@RequestParam(defaultValue = "10") int limit) {
        return movieService.findFeatured(limit);
    }

    @GetMapping("/{id}")
    public MovieResponse findById(@PathVariable Long id) {
        return movieService.findById(id);
    }
}
