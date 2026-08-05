package com.cinema.booking.user;

/**
 * Phai khop 1-1 voi CHECK constraint tren cot users.role (xem V1__init_schema.sql).
 * Dung EnumType.STRING khi map JPA de luu dung ten nay vao DB, khong luu ordinal.
 */
public enum UserRole {
    USER,
    ADMIN
}
