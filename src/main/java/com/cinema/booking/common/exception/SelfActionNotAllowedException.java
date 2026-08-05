package com.cinema.booking.common.exception;

// Nem exception nay khi admin thao tac khoa/doi role len chinh minh
// (chan self-lockout). GlobalExceptionHandler bat va tra ve HTTP 409.
public class SelfActionNotAllowedException extends RuntimeException {

    public SelfActionNotAllowedException(String message) {
        super(message);
    }
}
