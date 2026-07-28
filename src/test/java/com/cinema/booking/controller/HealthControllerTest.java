package com.cinema.booking.controller;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test thuong (khong dung @SpringBootTest) nen KHONG can Postgres/Redis
 * dang chay van "mvn test" duoc. Cac test can Spring context that
 * (vi du query DB) se duoc them tu Tuan 2 bang Testcontainers.
 */
class HealthControllerTest {

    @Test
    void health_returnsUpStatus() {
        HealthController controller = new HealthController();
        Map<String, String> result = controller.health();

        assertEquals("UP", result.get("status"));
        assertEquals("booking-service", result.get("service"));
    }
}
