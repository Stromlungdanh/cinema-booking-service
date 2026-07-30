package com.cinema.booking.cinema;

import com.cinema.booking.brand.Brand;
import com.cinema.booking.brand.BrandRepository;
import com.cinema.booking.cinema.dto.CinemaRequest;
import com.cinema.booking.cinema.dto.CinemaResponse;
import com.cinema.booking.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CinemaService {

    private final CinemaRepository cinemaRepository;
    private final BrandRepository brandRepository;

    @Transactional
    public CinemaResponse create(CinemaRequest request) {
        Brand brand = getBrandOrThrow(request.brandId());
        Cinema cinema = CinemaMapper.toEntity(brand, request);
        return CinemaMapper.toResponse(cinemaRepository.save(cinema));
    }

    @Transactional(readOnly = true)
    public List<CinemaResponse> findAll() {
        return cinemaRepository.findAll().stream()
                .map(CinemaMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CinemaResponse findById(Long id) {
        return CinemaMapper.toResponse(getCinemaOrThrow(id));
    }

    @Transactional
    public CinemaResponse update(Long id, CinemaRequest request) {
        Cinema cinema = getCinemaOrThrow(id);
        Brand brand = getBrandOrThrow(request.brandId());
        CinemaMapper.applyRequest(cinema, brand, request);
        return CinemaMapper.toResponse(cinema);
    }

    @Transactional
    public void delete(Long id) {
        cinemaRepository.delete(getCinemaOrThrow(id));
    }

    private Cinema getCinemaOrThrow(Long id) {
        return cinemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay rap voi id=" + id));
    }

    private Brand getBrandOrThrow(Long brandId) {
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay hang voi id=" + brandId));
    }
}
