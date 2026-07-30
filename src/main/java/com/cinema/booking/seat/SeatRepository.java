package com.cinema.booking.seat;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByRoomIdOrderByRowLabelAscColNumberAsc(Long roomId);

    void deleteByRoomId(Long roomId);
}
