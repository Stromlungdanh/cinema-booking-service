package com.cinema.booking.common.exception;

// Nem exception nay khi dang ky voi email da ton tai. GlobalExceptionHandler
// bat va tra ve HTTP 409.
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
