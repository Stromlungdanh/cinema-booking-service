package com.cinema.booking.cinema;

import com.cinema.booking.cinema.dto.CinemaRequest;
import com.cinema.booking.cinema.dto.CinemaResponse;
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
@RequestMapping("/api/admin/cinemas")
@RequiredArgsConstructor
public class CinemaController {

    private final CinemaService cinemaService;

    @PostMapping
    public ResponseEntity<CinemaResponse> create(@Valid @RequestBody CinemaRequest request) {
        CinemaResponse response = cinemaService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<CinemaResponse> findAll() {
        return cinemaService.findAll();
    }

    @GetMapping("/{id}")
    public CinemaResponse findById(@PathVariable Long id) {
        return cinemaService.findById(id);
    }

    @PutMapping("/{id}")
    public CinemaResponse update(@PathVariable Long id, @Valid @RequestBody CinemaRequest request) {
        return cinemaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        cinemaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
