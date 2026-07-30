# Cinema Booking System — Roadmap tổng quan

> File này trả lời 3 câu hỏi cho bất kỳ ai (kể cả tương lai của bạn) mở dự
> án ra: **Dự án này làm gì? Đang tới đâu rồi? Tiếp theo là gì?**
> Chi tiết từng task cụ thể (đã làm gì, tại sao, làm như thế nào) nằm ở
> [`PROGRESS_LOG.md`](./PROGRESS_LOG.md) — file này chỉ giữ bức tranh lớn.

## 1. Dự án là gì

**Cinema Booking System** — hệ thống đặt vé xem phim, học theo hướng
"đi từ đầu đến cuối một dự án thật": bắt đầu bằng monolith chạy được,
sau đó giải quyết các bài toán khó (concurrency, transaction, tách
microservice, deploy) giống một dự án production thực sự sẽ trải qua.

Người viết là backend Java chính, frontend chỉ làm đủ để có chỗ demo —
trọng tâm học là kiến trúc, dữ liệu, concurrency, và vận hành backend.

## 2. Mục tiêu chức năng

### Phía User
1. Xem phim (nổi bật / đang chiếu / sắp chiếu, tìm kiếm)
2. Xem rạp (theo hãng)
3. Xem phòng chiếu
4. Xem suất chiếu
5. Xem sơ đồ ghế
6. Chọn ghế
7. Đặt vé
8. Thanh toán (giả lập ở giai đoạn đầu)
9. Xem lịch sử đặt vé
10. Nhận ticket / mã QR

### Phía Admin
1. Quản lý phim
2. Quản lý rạp
3. Quản lý phòng
4. Quản lý ghế
5. Quản lý suất chiếu
6. Quản lý booking
7. Quản lý user

## 3. Luồng người dùng (UX flow) trên web

Trang chủ có 2 tab: **Chọn phim** và **Chọn rạp**. Cả hai đều dẫn tới
cùng một điểm cuối: chọn ghế → xác nhận → thanh toán → nhận ticket.

### Tab "Chọn phim"

| Màn hình | Nội dung |
|---|---|
| 1 — Trang chủ phim | Ô tìm phim/rạp; Phim nổi bật (bảng xếp hạng theo lượt xem: top 1, 2, 3...); Phim đang chiếu; Phim sắp chiếu |
| 2 — Chi tiết phim | Mô tả, thời lượng, ngôn ngữ, nội dung, diễn viên...; nút **"Mua vé"** |
| 3 — Chọn suất | Chọn ngày (mặc định hôm nay) → chọn hãng → chọn rạp (thuộc hãng đã chọn) → chọn suất chiếu (thuộc rạp đã chọn); nút **"Tiếp tục"** khi đủ 4 mục |
| 4 — Chọn ghế | Sơ đồ ghế, tính tiền realtime theo ghế đã chọn; nút **"Tiếp tục"** |
| 5 — Xác nhận & thanh toán | Tóm tắt (phim, rạp, giờ, ghế, phòng...); nút **"Thanh toán"** (giả lập) → nhận ticket/QR |

### Tab "Chọn rạp"

| Màn hình | Nội dung |
|---|---|
| 1 — Chọn hãng/rạp | List hãng chiếu phim → chọn hãng → list rạp thuộc hãng đó |
| 2 — Lịch chiếu của rạp | Chọn ngày (mặc định hôm nay); danh sách phim + suất chiếu trong ngày tại rạp đó |
| 3 — Chọn ghế | Sơ đồ ghế, tính tiền realtime |
| 4 — Xác nhận & thanh toán | Giống màn hình 5 của tab "Chọn phim" |

Hai luồng hội tụ ở bước **chọn ghế → thanh toán**, nên logic seat-map,
tính giá, giữ ghế tạm (Redis), và tạo booking nên dùng chung 1 bộ
API/service — không viết trùng cho 2 tab.

## 4. Tech stack và lý do dùng

| Công nghệ | Vai trò trong dự án |
|---|---|
| Java 21 + Spring Boot 3 | Core backend |
| PostgreSQL + Flyway | Dữ liệu quan hệ (phim, rạp, ghế, booking...); Flyway quản lý schema thay vì để Hibernate tự sinh — schema là nguồn sự thật duy nhất |
| Spring Security + JWT | Xác thực API, phân quyền User/Admin |
| Login Google / SSO | Học OAuth2/OIDC ngoài login thường (email/password) |
| Redis | Giữ ghế tạm có TTL (chống 2 người đặt trùng ghế), sau này có thể cache dữ liệu đọc nhiều (danh sách phim, suất chiếu) |
| Kafka | Giao tiếp bất đồng bộ giữa các service khi tách microservice (event: booking tạo, thanh toán thành công...) |
| ReactJS | Frontend tối thiểu để demo đủ luồng UX ở mục 3 |
| Docker / Docker Compose | Chạy Postgres, Redis, và sau này cả app + các service |
| GitHub Actions | CI/CD |

## 5. Các giai đoạn (phases)

Dự án đi theo thứ tự: **chạy được trước, đúng sau, nhanh/an toàn sau
cùng, tách nhỏ cuối cùng.** Đừng nhảy cóc sang giai đoạn sau khi giai
đoạn trước còn dang dở.

### Giai đoạn 1 — Monolith nền tảng (đang làm)
Mục tiêu: 1 Spring Boot app, 1 Postgres DB, đầy đủ CRUD cho các entity
tĩnh (Movie, Genre, Actor, Brand, Cinema, Room, SeatType, Seat), rồi
tới luồng nghiệp vụ chính (Showtime, Booking, Payment giả).

Checklist:
- [x] Docker Compose (Postgres + Redis), Flyway schema (V1) + seed (V2)
- [x] Spring Boot skeleton chạy được (`HealthController`)
- [x] Common exception handling (`ApiError`, `ResourceNotFoundException`, `GlobalExceptionHandler`)
- [x] CRUD Movie (`/api/admin/movies`)
- [x] CRUD Genre (`/api/admin/genres`)
- [x] CRUD Actor (`/api/admin/actors`)
- [x] Quan hệ nhiều-nhiều Movie ↔ Genre, Movie ↔ Actor (bảng `movie_genres`, `movie_actors`)
- [ ] CRUD Brand (`/api/admin/brands`)
- [ ] CRUD Cinema (`/api/admin/cinemas`, thuộc 1 brand)
- [ ] CRUD Room (thuộc 1 cinema)
- [ ] CRUD SeatType + sinh Seat theo sơ đồ phòng
- [ ] CRUD Showtime (gắn Movie + Room + khung giờ)
- [ ] API public cho User: danh sách phim nổi bật/đang chiếu/sắp chiếu, chi tiết phim, danh sách rạp theo hãng, suất chiếu theo rạp/ngày, sơ đồ ghế theo suất chiếu
- [ ] Luồng Booking (tạo booking từ ghế đã chọn, tính tiền, trạng thái PENDING/PAID/CANCELLED/EXPIRED)
- [ ] Payment giả lập (bypass, sinh `idempotency_key`) + sinh ticket/QR
- [ ] Spring Security + JWT (login thường), phân quyền `hasRole("ADMIN")` cho các controller admin
- [ ] Login Google / SSO

### Giai đoạn 2 — Bài toán khó: Concurrency & Transaction
Mục tiêu: chứng minh hệ thống không bán trùng ghế khi nhiều người đặt
cùng lúc — đây là phần "học được nhiều nhất" của giai đoạn monolith.
- [ ] Viết test giả lập nhiều thread/user đặt cùng 1 ghế cùng lúc
- [ ] Áp dụng lock (pessimistic lock DB, hoặc optimistic lock + retry) cho bước tạo `booking_seats`
- [ ] Redis giữ ghế tạm có TTL (seat-hold) trước khi thanh toán xong
- [ ] Partial unique index trên `booking_seats(showtime_id, seat_id)` giới hạn ở booking `PAID` (xem `docs/ERD.md` mục "Deferred constraints")

### Giai đoạn 3 — File & Payment thật hơn
- [ ] Upload ảnh (poster phim, avatar diễn viên, logo brand) — lưu local hoặc S3-compatible storage
- [ ] Xuất PDF vé
- [ ] Tích hợp payment sandbox thật (VD: VNPay/Momo sandbox, hoặc Stripe test mode)
- [ ] Xử lý idempotency cho API thanh toán (đã có cột `idempotency_key`, cần logic dùng nó đúng cách)

### Giai đoạn 4 — Tách Microservice + Saga (bài học system design giá trị nhất)
- [ ] Xác định biên service (VD: `movie-service`, `cinema-service`, `booking-service`, `payment-service`, `user-service`)
- [ ] Kafka cho giao tiếp bất đồng bộ giữa service
- [ ] Saga pattern xử lý transaction xuyên service (VD: đặt vé → giữ ghế → thanh toán → xác nhận, cần rollback/compensate khi 1 bước fail)
- [ ] Centralized logging (VD: ELK/Loki) để trace 1 request đi qua nhiều service

### Giai đoạn 5 — Deploy & CI/CD
- [ ] Docker hóa toàn bộ service
- [ ] GitHub Actions: build, test, (sau này) build image
- [ ] Deploy lên cloud (chọn platform cụ thể khi tới giai đoạn này)

## 6. Đang làm tới đâu (snapshot)

Đang ở **Giai đoạn 1**. Đã có CRUD cho 3 entity tĩnh: `Movie`, `Genre`,
`Actor`, và đã nối quan hệ nhiều-nhiều `Movie ↔ Genre` (`@ManyToMany`
thuần) và `Movie ↔ Actor` (entity liên kết `MovieCast` vì bảng
`movie_actors` mang thêm `role_name`). `mvn test` pass (31 test).

Xem chi tiết từng task, quyết định kỹ thuật, và lý do tại
[`PROGRESS_LOG.md`](./PROGRESS_LOG.md).

## 7. Sắp tới làm gì (ngay tiếp theo)

Quan hệ Movie↔Genre/Actor đã xong. Việc tiếp theo hợp lý nhất: rẽ sang
nhánh Brand → Cinema → Room → SeatType → Seat (để màn hình 3 — chọn
hãng/rạp — có dữ liệu thật). Đây là bước bắt buộc trước khi làm được
Showtime và Booking.

## 8. Quy ước cập nhật tài liệu này

- File này (`ROADMAP.md`) chỉ sửa khi: xong hẳn 1 mục trong checklist
  (tick `[x]`), đổi hướng/quyết định kiến trúc, hoặc chuyển sang giai
  đoạn mới. Không ghi chi tiết từng file đã tạo ở đây.
- Mọi chi tiết "đã làm gì, như thế nào, tại sao" ghi vào
  `PROGRESS_LOG.md`, không lặp lại ở đây.
