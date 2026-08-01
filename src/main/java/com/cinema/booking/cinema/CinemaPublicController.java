package com.cinema.booking.cinema;

import com.cinema.booking.cinema.dto.CinemaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// API cong khai cho man hinh User (tab "Chon rap" - man hinh 1: chon hang
// roi list rap thuoc hang do).
@RestController
@RequestMapping("/api/brands/{brandId}/cinemas")
@RequiredArgsConstructor
public class CinemaPublicController {

    private final CinemaService cinemaService;

    @GetMapping
    public List<CinemaResponse> findByBrand(@PathVariable Long brandId) {
        return cinemaService.findByBrand(brandId);
    }
}
