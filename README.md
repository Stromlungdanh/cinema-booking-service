# Cinema Booking System - booking-service (Giai đoạn 1)

Base project cho Tuần 1, Ngày 1–2: Docker Compose (Postgres + Redis), ERD,
Flyway migration, Spring Boot skeleton chạy được ngay.

## Yêu cầu môi trường

- Java 21 (`java -version` để kiểm tra)
- Maven 3.9+ (`mvn -version`)
- Docker + Docker Compose (`docker --version`)

## Cấu trúc thư mục

```
booking-service/
├── docker-compose.yml          # Postgres + Redis
├── .env.example                # copy thành .env
├── docs/ERD.md                 # sơ đồ dữ liệu (mermaid, xem trực tiếp trên GitHub)
├── pom.xml
└── src/main/
    ├── java/com/cinema/booking/
    │   ├── CinemaBookingApplication.java
    │   └── controller/HealthController.java
    └── resources/
        ├── application.yml
        └── db/migration/        # Flyway: V1 = schema, V2 = seed data
```

## Chạy lần đầu

```bash
# 1. Copy file env
cp .env.example .env

# 2. Bật Postgres + Redis
docker compose up -d

# 3. Kiểm tra container đã "healthy" (đợi vài giây)
docker compose ps

# 4. Chạy app (Flyway sẽ tự động apply V1 + V2 khi Spring Boot khởi động)
mvn spring-boot:run
```

## Kiểm tra mọi thứ đã chạy đúng

```bash
curl http://localhost:8080/api/health
# -> {"status":"UP","service":"booking-service"}
```

Swagger UI (trống, chưa có API nghiệp vụ — sẽ có từ Ngày 3-4):
http://localhost:8080/swagger-ui.html

Kiểm tra schema đã được Flyway tạo đúng (dùng psql, hoặc DBeaver/TablePlus):

```bash
docker exec -it cinema-postgres psql -U cinema_user -d cinema_booking -c "\dt"
docker exec -it cinema-postgres psql -U cinema_user -d cinema_booking -c "SELECT * FROM seat_types;"
```

Bạn sẽ thấy 14 bảng (users, brands, cinemas, rooms, seat_types, seats,
movies, genres, movie_genres, actors, movie_actors, showtimes, bookings,
booking_seats, payments) + bảng `flyway_schema_history` do Flyway tự tạo để
theo dõi migration đã chạy.

## Chạy test

```bash
mvn test
```

Test hiện tại không cần Postgres/Redis đang chạy (xem giải thích trong
`HealthControllerTest`). Từ Tuần 2 sẽ thêm Testcontainers cho các test cần
DB thật.

## Đưa lên GitHub

```bash
git init
git add .
git commit -m "chore: base project - docker compose, ERD, flyway schema"

# Tạo repo trống trên github.com trước (không tick "Add README"), rồi:
git remote add origin https://github.com/<username>/<repo-name>.git
git branch -M main
git push -u origin main
```

⚠️ File `.env` (chứa mật khẩu thật) đã nằm trong `.gitignore` — chỉ
`.env.example` được commit. Kiểm tra lại bằng `git status` trước khi commit
để chắc chắn `.env` không bị add nhầm.

## Tiếp theo (Ngày 3-4)

Viết Entity + Repository (Spring Data JPA) cho Movie, Genre, Actor, Brand,
Cinema, Room, SeatType, Seat + API CRUD (GET trước). Xem `docs/ERD.md` để
đối chiếu tên cột khi viết Entity.
