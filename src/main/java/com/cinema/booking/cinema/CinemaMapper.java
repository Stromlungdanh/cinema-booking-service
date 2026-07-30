package com.cinema.booking.cinema;

import com.cinema.booking.brand.Brand;
import com.cinema.booking.cinema.dto.CinemaRequest;
import com.cinema.booking.cinema.dto.CinemaResponse;

// Mapper khong tu query DB - Service resolve Brand entity truoc, mapper chi lap rap
// (cung quy uoc voi MovieMapper).
public final class CinemaMapper {

    private CinemaMapper() {
    }

    public static Cinema toEntity(Brand brand, CinemaRequest request) {
        Cinema cinema = new Cinema();
        applyRequest(cinema, brand, request);
        return cinema;
    }

    public static void applyRequest(Cinema cinema, Brand brand, CinemaRequest request) {
        cinema.setBrand(brand);
        cinema.setName(request.name());
        cinema.setAddress(request.address());
        cinema.setCity(request.city());
    }

    public static CinemaResponse toResponse(Cinema cinema) {
        return new CinemaResponse(
                cinema.getId(),
                cinema.getBrand().getId(),
                cinema.getBrand().getName(),
                cinema.getName(),
                cinema.getAddress(),
                cinema.getCity()
        );
    }
}
