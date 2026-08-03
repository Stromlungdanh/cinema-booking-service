-- ============================================================
-- V3: Seed du lieu test cho Postman/Swagger - KHONG dung cho
-- production. Dung subquery theo natural key (ten) thay vi hardcode id
-- de khong phu thuoc thu tu sinh IDENTITY.
--
-- Showtime dung CURRENT_DATE (hom nay) va CURRENT_DATE + 1 (ngay mai) o
-- gio VN co dinh (Asia/Ho_Chi_Minh) de luon test duoc man hinh "chon
-- ngay = hom nay" bat ke khi nao migration nay chay. Luu y: vi Flyway
-- chi chay 1 lan, neu DB song qua nhieu ngay khong reset thi cac suat
-- chieu nay se "troi" ve qua khu - can seed lai (VD: xoa volume Docker)
-- neu muon test lai voi ngay hom nay/ngay mai.
-- ============================================================

-- ---------- Brand + Cinema + Room ----------

INSERT INTO brands (name, logo_url) VALUES
    ('CGV Cinemas', 'https://picsum.photos/seed/cgv-logo/200'),
    ('BHD Star', 'https://picsum.photos/seed/bhd-logo/200');

INSERT INTO cinemas (brand_id, name, address, city)
SELECT id, 'CGV Landmark 81', '720A Dien Bien Phu, Binh Thanh', 'Ho Chi Minh'
FROM brands WHERE name = 'CGV Cinemas';

INSERT INTO cinemas (brand_id, name, address, city)
SELECT id, 'BHD Star Bitexco', '2 Hai Trieu, Quan 1', 'Ho Chi Minh'
FROM brands WHERE name = 'BHD Star';

INSERT INTO rooms (cinema_id, name, room_type)
SELECT id, 'Phong 1', '2D' FROM cinemas WHERE name = 'CGV Landmark 81';

INSERT INTO rooms (cinema_id, name, room_type)
SELECT id, 'Phong 1', 'IMAX' FROM cinemas WHERE name = 'BHD Star Bitexco';

-- ---------- Seat layout: 5 hang (A-E) x 8 cot cho moi phong tren ----------
-- A-B: STANDARD, C-D: VIP, E: COUPLE - dung generate_series giong cach
-- SeatService.generateLayout sinh o tang ung dung.

INSERT INTO seats (room_id, row_label, col_number, seat_type_id)
SELECT r.id, g.row_label, gc.col_number, st.id
FROM rooms r
JOIN cinemas c ON c.id = r.cinema_id AND c.name IN ('CGV Landmark 81', 'BHD Star Bitexco') AND r.name = 'Phong 1'
CROSS JOIN (VALUES ('A'), ('B'), ('C'), ('D'), ('E')) AS g(row_label)
CROSS JOIN generate_series(1, 8) AS gc(col_number)
JOIN seat_types st ON st.name = CASE
    WHEN g.row_label IN ('A', 'B') THEN 'STANDARD'
    WHEN g.row_label IN ('C', 'D') THEN 'VIP'
    ELSE 'COUPLE'
END;

-- ---------- Genre + Actor ----------

INSERT INTO genres (name) VALUES
    ('Hanh Dong'), ('Hai'), ('Khoa Hoc Vien Tuong'), ('Kinh Di'), ('Hoat Hinh'), ('Tinh Cam');

INSERT INTO actors (name, avatar_url) VALUES
    ('Robert Downey Jr.', 'https://picsum.photos/seed/actor-rdj/200'),
    ('Scarlett Johansson', 'https://picsum.photos/seed/actor-sj/200'),
    ('Ryan Reynolds', 'https://picsum.photos/seed/actor-rr/200'),
    ('Hugh Jackman', 'https://picsum.photos/seed/actor-hj/200'),
    ('Timothee Chalamet', 'https://picsum.photos/seed/actor-tc/200'),
    ('Zendaya', 'https://picsum.photos/seed/actor-z/200');

-- ---------- Movie ----------

INSERT INTO movies (title, description, duration_min, language, release_date, poster_url, trailer_url, status, view_count) VALUES
    ('Avengers: Doomsday', 'Cac sieu anh hung hop suc doi dau moi de doa toan cau.', 165, 'English', CURRENT_DATE - 14,
        'https://picsum.photos/seed/movie-avengers/400/600', 'https://example.com/trailer/avengers-doomsday', 'NOW_SHOWING', 5000),
    ('Deadpool & Wolverine 2', 'Bo doi an y tro lai voi phong cach hai huoc dac trung.', 128, 'English', CURRENT_DATE - 30,
        'https://picsum.photos/seed/movie-deadpool/400/600', 'https://example.com/trailer/deadpool-wolverine-2', 'NOW_SHOWING', 4200),
    ('Inside Out 3', 'Hanh trinh cam xuc tiep tuc voi nhung nhan vat moi.', 100, 'English', CURRENT_DATE - 7,
        'https://picsum.photos/seed/movie-insideout/400/600', 'https://example.com/trailer/inside-out-3', 'NOW_SHOWING', 3100),
    ('Spider-Man: Beyond the Spider-Verse', 'Phan tiep theo cua vu tru nhen hoat hinh.', 140, 'English', CURRENT_DATE + 30,
        'https://picsum.photos/seed/movie-spiderman/400/600', 'https://example.com/trailer/spiderman-beyond', 'COMING_SOON', 1200),
    ('Dune: Part Three', 'Phan ket cua bo ba Dune chuyen the tu tieu thuyet.', 170, 'English', CURRENT_DATE + 60,
        'https://picsum.photos/seed/movie-dune3/400/600', 'https://example.com/trailer/dune-part-three', 'COMING_SOON', 2600),
    ('The Haunting Hour', 'Phim kinh di da ket thuc thoi gian chieu.', 95, 'English', CURRENT_DATE - 200,
        'https://picsum.photos/seed/movie-haunting/400/600', 'https://example.com/trailer/haunting-hour', 'ENDED', 800);

-- ---------- Movie <-> Genre ----------

INSERT INTO movie_genres (movie_id, genre_id)
SELECT m.id, g.id FROM movies m, genres g WHERE m.title = 'Avengers: Doomsday' AND g.name = 'Hanh Dong'
UNION ALL
SELECT m.id, g.id FROM movies m, genres g WHERE m.title = 'Deadpool & Wolverine 2' AND g.name = 'Hanh Dong'
UNION ALL
SELECT m.id, g.id FROM movies m, genres g WHERE m.title = 'Deadpool & Wolverine 2' AND g.name = 'Hai'
UNION ALL
SELECT m.id, g.id FROM movies m, genres g WHERE m.title = 'Inside Out 3' AND g.name = 'Hoat Hinh'
UNION ALL
SELECT m.id, g.id FROM movies m, genres g WHERE m.title = 'Spider-Man: Beyond the Spider-Verse' AND g.name = 'Hoat Hinh'
UNION ALL
SELECT m.id, g.id FROM movies m, genres g WHERE m.title = 'Spider-Man: Beyond the Spider-Verse' AND g.name = 'Hanh Dong'
UNION ALL
SELECT m.id, g.id FROM movies m, genres g WHERE m.title = 'Dune: Part Three' AND g.name = 'Khoa Hoc Vien Tuong'
UNION ALL
SELECT m.id, g.id FROM movies m, genres g WHERE m.title = 'The Haunting Hour' AND g.name = 'Kinh Di';

-- ---------- Movie <-> Actor (cast) ----------

INSERT INTO movie_actors (movie_id, actor_id, role_name)
SELECT m.id, a.id, 'Tony Stark' FROM movies m, actors a WHERE m.title = 'Avengers: Doomsday' AND a.name = 'Robert Downey Jr.'
UNION ALL
SELECT m.id, a.id, 'Natasha Romanoff' FROM movies m, actors a WHERE m.title = 'Avengers: Doomsday' AND a.name = 'Scarlett Johansson'
UNION ALL
SELECT m.id, a.id, 'Wade Wilson / Deadpool' FROM movies m, actors a WHERE m.title = 'Deadpool & Wolverine 2' AND a.name = 'Ryan Reynolds'
UNION ALL
SELECT m.id, a.id, 'Logan / Wolverine' FROM movies m, actors a WHERE m.title = 'Deadpool & Wolverine 2' AND a.name = 'Hugh Jackman'
UNION ALL
SELECT m.id, a.id, 'Paul Atreides' FROM movies m, actors a WHERE m.title = 'Dune: Part Three' AND a.name = 'Timothee Chalamet'
UNION ALL
SELECT m.id, a.id, 'Chani' FROM movies m, actors a WHERE m.title = 'Dune: Part Three' AND a.name = 'Zendaya';

-- ---------- Showtime: hom nay + ngay mai, gio VN co dinh (+07:00) ----------

INSERT INTO showtimes (movie_id, room_id, start_time, end_time, base_price)
SELECT m.id, r.id,
       (CURRENT_DATE + TIME '10:00:00') AT TIME ZONE 'Asia/Ho_Chi_Minh',
       (CURRENT_DATE + TIME '12:45:00') AT TIME ZONE 'Asia/Ho_Chi_Minh',
       85000
FROM movies m, rooms r, cinemas c
WHERE m.title = 'Avengers: Doomsday' AND r.name = 'Phong 1' AND r.cinema_id = c.id AND c.name = 'CGV Landmark 81'
UNION ALL
SELECT m.id, r.id,
       (CURRENT_DATE + TIME '19:00:00') AT TIME ZONE 'Asia/Ho_Chi_Minh',
       (CURRENT_DATE + TIME '21:45:00') AT TIME ZONE 'Asia/Ho_Chi_Minh',
       120000
FROM movies m, rooms r, cinemas c
WHERE m.title = 'Avengers: Doomsday' AND r.name = 'Phong 1' AND r.cinema_id = c.id AND c.name = 'BHD Star Bitexco'
UNION ALL
SELECT m.id, r.id,
       (CURRENT_DATE + TIME '13:00:00') AT TIME ZONE 'Asia/Ho_Chi_Minh',
       (CURRENT_DATE + TIME '15:15:00') AT TIME ZONE 'Asia/Ho_Chi_Minh',
       85000
FROM movies m, rooms r, cinemas c
WHERE m.title = 'Deadpool & Wolverine 2' AND r.name = 'Phong 1' AND r.cinema_id = c.id AND c.name = 'CGV Landmark 81'
UNION ALL
SELECT m.id, r.id,
       (CURRENT_DATE + TIME '16:00:00') AT TIME ZONE 'Asia/Ho_Chi_Minh',
       (CURRENT_DATE + TIME '17:45:00') AT TIME ZONE 'Asia/Ho_Chi_Minh',
       95000
FROM movies m, rooms r, cinemas c
WHERE m.title = 'Inside Out 3' AND r.name = 'Phong 1' AND r.cinema_id = c.id AND c.name = 'BHD Star Bitexco'
UNION ALL
SELECT m.id, r.id,
       (CURRENT_DATE + 1 + TIME '10:00:00') AT TIME ZONE 'Asia/Ho_Chi_Minh',
       (CURRENT_DATE + 1 + TIME '12:45:00') AT TIME ZONE 'Asia/Ho_Chi_Minh',
       85000
FROM movies m, rooms r, cinemas c
WHERE m.title = 'Avengers: Doomsday' AND r.name = 'Phong 1' AND r.cinema_id = c.id AND c.name = 'CGV Landmark 81'
UNION ALL
SELECT m.id, r.id,
       (CURRENT_DATE + 1 + TIME '19:00:00') AT TIME ZONE 'Asia/Ho_Chi_Minh',
       (CURRENT_DATE + 1 + TIME '21:00:00') AT TIME ZONE 'Asia/Ho_Chi_Minh',
       120000
FROM movies m, rooms r, cinemas c
WHERE m.title = 'Deadpool & Wolverine 2' AND r.name = 'Phong 1' AND r.cinema_id = c.id AND c.name = 'BHD Star Bitexco';
