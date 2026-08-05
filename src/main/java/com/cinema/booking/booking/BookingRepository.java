package com.cinema.booking.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 1 query xu ly moi to hop filter optional (status/userId/showtimeId)
    // thay vi no derived-query method cho tung to hop - dung cho Admin
    // quan ly Booking (GET /api/admin/bookings).
    @Query("SELECT b FROM Booking b WHERE "
            + "(:status IS NULL OR b.status = :status) AND "
            + "(:userId IS NULL OR b.user.id = :userId) AND "
            + "(:showtimeId IS NULL OR b.showtime.id = :showtimeId) "
            + "ORDER BY b.createdAt DESC")
    List<Booking> findAllFiltered(@Param("status") BookingStatus status,
                                   @Param("userId") Long userId,
                                   @Param("showtimeId") Long showtimeId);
}
