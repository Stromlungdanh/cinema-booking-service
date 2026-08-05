package com.cinema.booking.common.exception;

// Nem exception nay khi dang nhap sai email/mat khau. GlobalExceptionHandler
// bat va tra ve HTTP 401.
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
