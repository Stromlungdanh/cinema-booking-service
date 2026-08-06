package com.cinema.booking.common.exception;

// Nem exception nay khi token quen mat khau khong ton tai, het han,
// hoac da duoc su dung. GlobalExceptionHandler bat va tra ve HTTP 400.
public class InvalidResetTokenException extends RuntimeException {

    public InvalidResetTokenException(String message) {
        super(message);
    }
}
