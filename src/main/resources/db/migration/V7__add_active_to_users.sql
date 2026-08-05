-- ============================================================
-- V7: Them cot "active" cho users - phuc vu Admin quan ly User
-- (khoa/mo tai khoan). Mac dinh true - moi user hien co (seed +
-- da dang ky qua /api/auth/register) deu con hoat dong binh thuong.
-- ============================================================

ALTER TABLE users ADD COLUMN active BOOLEAN NOT NULL DEFAULT true;
