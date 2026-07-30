package com.cinema.booking.brand;

import com.cinema.booking.brand.dto.BrandRequest;
import com.cinema.booking.brand.dto.BrandResponse;

public final class BrandMapper {

    private BrandMapper() {
    }

    public static Brand toEntity(BrandRequest request) {
        Brand brand = new Brand();
        applyRequest(brand, request);
        return brand;
    }

    public static void applyRequest(Brand brand, BrandRequest request) {
        brand.setName(request.name());
        brand.setLogoUrl(request.logoUrl());
    }

    public static BrandResponse toResponse(Brand brand) {
        return new BrandResponse(brand.getId(), brand.getName(), brand.getLogoUrl());
    }
}
