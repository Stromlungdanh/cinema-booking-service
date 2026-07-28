# ERD - Cinema Booking System (Giai đoạn 1: Monolith)

Sơ đồ dưới đây dùng cú pháp Mermaid — GitHub sẽ tự render thành hình khi bạn
mở file này trên web (không cần cài gì thêm). Nếu xem trong VS Code, cài
extension "Markdown Preview Mermaid Support" để thấy hình khi preview.

```mermaid
erDiagram
    USERS ||--o{ BOOKINGS : places
    BRANDS ||--o{ CINEMAS : has
    CINEMAS ||--o{ ROOMS : has
    ROOMS ||--o{ SEATS : has
    SEAT_TYPES ||--o{ SEATS : classifies
    ROOMS ||--o{ SHOWTIMES : hosts
    MOVIES ||--o{ SHOWTIMES : scheduled_as
    MOVIES ||--o{ MOVIE_GENRES : has
    GENRES ||--o{ MOVIE_GENRES : categorizes
    MOVIES ||--o{ MOVIE_ACTORS : casts
    ACTORS ||--o{ MOVIE_ACTORS : plays_in
    SHOWTIMES ||--o{ BOOKINGS : booked_for
    BOOKINGS ||--o{ BOOKING_SEATS : contains
    SEATS ||--o{ BOOKING_SEATS : reserved_as
    BOOKINGS ||--o| PAYMENTS : paid_by

    USERS {
        bigint id PK
        varchar name
        varchar email UK
        varchar password_hash
        varchar provider
        varchar provider_id
        varchar role
        timestamptz created_at
    }
    BRANDS {
        bigint id PK
        varchar name
        varchar logo_url
    }
    CINEMAS {
        bigint id PK
        bigint brand_id FK
        varchar name
        varchar address
        varchar city
    }
    ROOMS {
        bigint id PK
        bigint cinema_id FK
        varchar name
        varchar room_type
    }
    SEAT_TYPES {
        bigint id PK
        varchar name
        numeric price_multiplier
    }
    SEATS {
        bigint id PK
        bigint room_id FK
        varchar row_label
        int col_number
        bigint seat_type_id FK
    }
    MOVIES {
        bigint id PK
        varchar title
        text description
        int duration_min
        varchar language
        date release_date
        varchar poster_url
        varchar trailer_url
        varchar status
        bigint view_count
    }
    GENRES {
        bigint id PK
        varchar name
    }
    MOVIE_GENRES {
        bigint movie_id FK
        bigint genre_id FK
    }
    ACTORS {
        bigint id PK
        varchar name
        varchar avatar_url
    }
    MOVIE_ACTORS {
        bigint movie_id FK
        bigint actor_id FK
        varchar role_name
    }
    SHOWTIMES {
        bigint id PK
        bigint movie_id FK
        bigint room_id FK
        timestamptz start_time
        timestamptz end_time
        numeric base_price
    }
    BOOKINGS {
        bigint id PK
        bigint user_id FK
        bigint showtime_id FK
        varchar status
        numeric total_price
        timestamptz created_at
    }
    BOOKING_SEATS {
        bigint id PK
        bigint booking_id FK
        bigint seat_id FK
        numeric price
    }
    PAYMENTS {
        bigint id PK
        bigint booking_id FK
        varchar method
        varchar status
        varchar transaction_ref
        varchar idempotency_key UK
        timestamptz created_at
    }
```

## Giải thích quan hệ chính

- **Brand → Cinema → Room → Seat**: 1 hãng (VD: BHD) có nhiều rạp (chi nhánh),
  1 rạp có nhiều phòng chiếu, 1 phòng có nhiều ghế.
- **Movie ↔ Genre**, **Movie ↔ Actor**: quan hệ nhiều-nhiều qua bảng trung gian
  `movie_genres` / `movie_actors`.
- **Showtime**: gắn 1 Movie với 1 Room tại 1 khung giờ cụ thể — đây là đối
  tượng trung tâm mà user chọn để xem sơ đồ ghế.
- **Booking → BookingSeat**: 1 booking có thể chứa nhiều ghế (đi nhóm bạn/gia
  đình), mỗi `BookingSeat` là 1 dòng vé với giá tại thời điểm đặt (không tham
  chiếu ngược `base_price` phòng khi giá thay đổi sau này).
- **Booking → Payment**: quan hệ 1-1 (mỗi booking có tối đa 1 payment thành
  công; `idempotency_key` là unique để chống double-charge khi client gọi lại
  API thanh toán).

## Có chủ đích KHÔNG có trong ERD này

- **Seat-hold (giữ ghế tạm)**: không phải bảng trong Postgres — sẽ là key
  Redis dạng `seat:{showtimeId}:{seatId}` có TTL, làm ở Tuần 3. Bản chất dữ
  liệu tạm thời/ephemeral nên không thuộc schema quan hệ.
- **Roles chi tiết (permission-based)**: hiện dùng 1 cột `role` đơn giản
  (`USER`/`ADMIN`) trong `users`, đủ cho Giai đoạn 1. Tách bảng `roles` +
  `permissions` riêng chỉ cần khi hệ thống phân quyền phức tạp hơn.

## Deferred constraints (sẽ thêm sau, không phải bây giờ)

- **Chống bán trùng ghế ở tầng DB**: một partial unique index trên
  `booking_seats(showtime_id, seat_id)` giới hạn trong các booking đã `PAID`
  — đóng vai trò "lưới an toàn cuối cùng" phía sau Redis seat-hold. Cần thêm
  cột `showtime_id` (denormalize) vào `booking_seats` trước khi làm — để dành
  cho Tuần 3, lúc đó business logic concurrency đã rõ ràng hơn.
