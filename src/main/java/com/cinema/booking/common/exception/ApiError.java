package com.cinema.booking.common.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        Instant timestamp,
        int status,
        String message,
        Map<String, String> fieldErrors
) {
    public static ApiError of(int status, String message) {
        return new ApiError(Instant.now(), status, message, null);
    }

    public static ApiError of(int status, String message, Map<String, String> fieldErrors) {
        return new ApiError(Instant.now(), status, message, fieldErrors);
    }
}
