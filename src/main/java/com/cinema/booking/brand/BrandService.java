package com.cinema.booking.brand;

import com.cinema.booking.brand.dto.BrandRequest;
import com.cinema.booking.brand.dto.BrandResponse;
import com.cinema.booking.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    @Transactional
    public BrandResponse create(BrandRequest request) {
        Brand brand = BrandMapper.toEntity(request);
        return BrandMapper.toResponse(brandRepository.save(brand));
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> findAll() {
        return brandRepository.findAll().stream()
                .map(BrandMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BrandResponse findById(Long id) {
        return BrandMapper.toResponse(getBrandOrThrow(id));
    }

    @Transactional
    public BrandResponse update(Long id, BrandRequest request) {
        Brand brand = getBrandOrThrow(id);
        BrandMapper.applyRequest(brand, request);
        return BrandMapper.toResponse(brand);
    }

    @Transactional
    public void delete(Long id) {
        brandRepository.delete(getBrandOrThrow(id));
    }

    private Brand getBrandOrThrow(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Khong tim thay hang voi id=" + id));
    }
}
