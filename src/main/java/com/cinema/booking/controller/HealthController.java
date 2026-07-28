package com.cinema.booking.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint kiem tra nhanh: app da chay, da ket noi duoc datasource,
 * Flyway da migrate xong chua bi loi khi startup.
 * Goi: GET http://localhost:8080/api/health
 */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "service", "booking-service"
        );
    }
}
