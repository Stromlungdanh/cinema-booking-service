package com.cinema.booking.movie;

/**
 * Phai khop 1-1 voi CHECK constraint tren cot movies.status (xem V1__init_schema.sql).
 * Dung EnumType.STRING khi map JPA de luu dung ten nay vao DB, khong luu ordinal.
 */
public enum MovieStatus {
    NOW_SHOWING,
    COMING_SOON,
    ENDED
}
