package com.cinema.booking.booking;

import com.cinema.booking.booking.dto.BookingRequest;
import com.cinema.booking.booking.dto.BookingResponse;
import com.cinema.booking.booking.dto.TicketResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public BookingResponse findById(@PathVariable Long id) {
        return bookingService.findById(id);
    }

    @GetMapping
    public List<BookingResponse> findByUser(@RequestParam Long userId) {
        return bookingService.findByUser(userId);
    }

    @PatchMapping("/{id}/cancel")
    public BookingResponse cancel(@PathVariable Long id) {
        return bookingService.cancel(id);
    }

    @GetMapping("/{id}/ticket")
    public TicketResponse getTicket(@PathVariable Long id) {
        return bookingService.getTicket(id);
    }
}
