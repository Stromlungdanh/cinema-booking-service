package com.cinema.booking.seat;

import com.cinema.booking.seat.dto.SeatLayoutRequest;
import com.cinema.booking.seat.dto.SeatResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Long duoi Room (khac cac controller CRUD chuan khac) vi Seat khong co y
// nghia doc lap ngoai ngu canh 1 Room - khong co danh sach "tat ca ghe" toan
// he thong can xem.
@RestController
@RequestMapping("/api/admin/rooms/{roomId}/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @PostMapping
    public ResponseEntity<List<SeatResponse>> generateLayout(
            @PathVariable Long roomId, @Valid @RequestBody SeatLayoutRequest request) {
        List<SeatResponse> response = seatService.generateLayout(roomId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<SeatResponse> findByRoom(@PathVariable Long roomId) {
        return seatService.findByRoom(roomId);
    }
}
