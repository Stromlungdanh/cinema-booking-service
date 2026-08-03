package com.cinema.booking.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {

    // Ghe da bi giu boi 1 booking dang PENDING/PAID cho cung suat chieu -
    // check app-level, chua chong duoc race condition (danh cho Giai doan 2
    // khi lam lock/Redis seat-hold).
    @Query("select bs.seat.id from BookingSeat bs " +
            "where bs.booking.showtime.id = :showtimeId " +
            "and bs.booking.status in :statuses " +
            "and bs.seat.id in :seatIds")
    List<Long> findBookedSeatIds(@Param("showtimeId") Long showtimeId,
                                  @Param("statuses") List<BookingStatus> statuses,
                                  @Param("seatIds") List<Long> seatIds);
}
