package com.cinema.booking.common.exception;

// Nem exception nay o Service khi khong tim thay entity theo id.
// GlobalExceptionHandler se bat va tra ve HTTP 404, controller khong can biet gi ve HTTP.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
