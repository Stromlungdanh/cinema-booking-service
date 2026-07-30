package com.cinema.booking.seattype;

import com.cinema.booking.seattype.dto.SeatTypeRequest;
import com.cinema.booking.seattype.dto.SeatTypeResponse;
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
@RequestMapping("/api/admin/seat-types")
@RequiredArgsConstructor
public class SeatTypeController {

    private final SeatTypeService seatTypeService;

    @PostMapping
    public ResponseEntity<SeatTypeResponse> create(@Valid @RequestBody SeatTypeRequest request) {
        SeatTypeResponse response = seatTypeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<SeatTypeResponse> findAll() {
        return seatTypeService.findAll();
    }

    @GetMapping("/{id}")
    public SeatTypeResponse findById(@PathVariable Long id) {
        return seatTypeService.findById(id);
    }

    @PutMapping("/{id}")
    public SeatTypeResponse update(@PathVariable Long id, @Valid @RequestBody SeatTypeRequest request) {
        return seatTypeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        seatTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
