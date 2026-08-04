-- ============================================================
-- V5: Them cot ticket_code cho bookings, dung khi thanh toan (gia lap)
-- thanh cong - sinh ma ve duy nhat de encode vao QR (xem package
-- `payment`). Booking chua thanh toan (PENDING/CANCELLED/EXPIRED) co
-- ticket_code = NULL.
-- ============================================================

ALTER TABLE bookings
    ADD COLUMN ticket_code VARCHAR(50);

ALTER TABLE bookings
    ADD CONSTRAINT uq_bookings_ticket_code UNIQUE (ticket_code);
