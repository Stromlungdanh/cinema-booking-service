-- ============================================================
-- V4: Seed 2 user demo cho test Luong Booking qua Postman - KHONG dung
-- cho production. `users` chua co API tao/list (chi la entity toi
-- thieu cho FK cua Booking), nen khong the tu dong lay id qua HTTP nhu
-- cac resource khac trong Postman collection - phai tra cuu 1 lan qua
-- psql/pgAdmin (xem huong dan trong description cua folder "Booking"
-- trong Postman collection) roi set thu cong vao collection variable
-- {{userId}}.
-- ============================================================

INSERT INTO users (name, email, provider, role) VALUES
    ('Nguyen Van A', 'nguyen.van.a@example.com', 'LOCAL', 'USER'),
    ('Tran Thi B', 'tran.thi.b@example.com', 'LOCAL', 'USER');
