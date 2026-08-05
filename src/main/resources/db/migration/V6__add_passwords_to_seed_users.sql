-- ============================================================
-- V6: Gan mat khau cho 2 user seed san (V4) + them 1 user ADMIN demo,
-- de test dang nhap/JWT/phan quyen ngay ma khong phai tu dang ky admin
-- (POST /api/auth/register luon tao role USER) hay sua DB tay.
--
-- Mat khau dev-only, CHI dung local:
--   nguyen.van.a@example.com / tran.thi.b@example.com -> Password123!
--   admin@example.com (ADMIN)                          -> AdminPass123!
-- Hash sinh bang BCryptPasswordEncoder (cung thu vien app dang dung).
-- ============================================================

UPDATE users
SET password_hash = '$2a$10$GGqaxAQBJDolv5OVpbDfO.7Ie1nwMHjAcHYcPKbKSHUnbzgI9Da7m'
WHERE email IN ('nguyen.van.a@example.com', 'tran.thi.b@example.com');

INSERT INTO users (name, email, password_hash, provider, role) VALUES
    ('Admin Demo', 'admin@example.com',
     '$2a$10$7qJi8mCnNQQuqJ1u/.ijS.voA6graXwMYhEFmy/gOxMw11QcPzb2W',
     'LOCAL', 'ADMIN');
