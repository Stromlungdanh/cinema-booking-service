package com.cinema.booking.brand;

import com.cinema.booking.brand.dto.BrandResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// API cong khai cho man hinh User (tab "Chon rap" - man hinh 1: chon hang).
@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandPublicController {

    private final BrandService brandService;

    @GetMapping
    public List<BrandResponse> findAll() {
        return brandService.findAll();
    }
}
