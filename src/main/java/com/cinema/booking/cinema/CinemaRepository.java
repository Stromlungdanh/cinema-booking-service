package com.cinema.booking.cinema;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CinemaRepository extends JpaRepository<Cinema, Long> {

    // Man hinh public: tab "Chon rap" - list rap thuoc 1 hang.
    List<Cinema> findByBrandId(Long brandId);
}
