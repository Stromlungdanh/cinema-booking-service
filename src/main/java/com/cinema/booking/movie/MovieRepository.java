package com.cinema.booking.movie;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    // Derived query method: Spring Data tu sinh SQL tu ten method, khong can viet @Query.
    // Se dung o man hinh public "phim dang chieu / sap chieu".
    List<Movie> findByStatus(MovieStatus status);
}
