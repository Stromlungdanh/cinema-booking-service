package com.cinema.booking.booking;

import com.cinema.booking.booking.dto.BookingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Quan ly booking phia Admin - xem toan bo booking (moi user), huy ho.
// Tach rieng khoi BookingController (/api/bookings, chi thao tac booking
// cua chinh user dang dang nhap) - dung tien le MovieController/
// MoviePublicController deu tach rieng du cung domain.
@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final BookingService bookingService;

    @GetMapping
    public List<BookingResponse> findAll(@RequestParam(required = false) BookingStatus status,
                                          @RequestParam(required = false) Long userId,
                                          @RequestParam(required = false) Long showtimeId) {
        return bookingService.findAllForAdmin(status, userId, showtimeId);
    }

    @GetMapping("/{id}")
    public BookingResponse findById(@PathVariable Long id) {
        return bookingService.findByIdForAdmin(id);
    }

    @PatchMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable Long id) {
        return bookingService.cancelForAdmin(id);
    }
}
