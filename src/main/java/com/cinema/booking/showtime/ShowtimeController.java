package com.cinema.booking.showtime;

import com.cinema.booking.showtime.dto.ShowtimeRequest;
import com.cinema.booking.showtime.dto.ShowtimeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    @PostMapping
    public ResponseEntity<ShowtimeResponse> create(@Valid @RequestBody ShowtimeRequest request) {
        ShowtimeResponse response = showtimeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<ShowtimeResponse> findAll() {
        return showtimeService.findAll();
    }

    @GetMapping("/{id}")
    public ShowtimeResponse findById(@PathVariable Long id) {
        return showtimeService.findById(id);
    }

    @PutMapping("/{id}")
    public ShowtimeResponse update(@PathVariable Long id, @Valid @RequestBody ShowtimeRequest request) {
        return showtimeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        showtimeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
