package com.cinema.booking.common.exception;

// Nem exception nay khi vi pham quy tac nghiep vu ve trang thai booking:
// ghe da bi giu boi booking khac (PENDING/PAID), hoac huy 1 booking khong
// con PENDING. GlobalExceptionHandler bat va tra ve HTTP 409.
public class BookingConflictException extends RuntimeException {

    public BookingConflictException(String message) {
        super(message);
    }
}
