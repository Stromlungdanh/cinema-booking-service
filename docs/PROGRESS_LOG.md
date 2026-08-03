# Nhật ký tiến độ chi tiết

> File này ghi **chi tiết từng task đã làm**: làm gì, để làm gì (mục
> đích), làm như thế nào (quyết định kỹ thuật đáng chú ý), và trạng thái
> test. Bức tranh tổng quan (mục tiêu, giai đoạn, sắp làm gì) nằm ở
> [`ROADMAP.md`](./ROADMAP.md) — đừng lặp lại nội dung đó ở đây.
>
> **Quy ước:** entry mới thêm vào **cuối file**, theo thứ tự thời gian
> tăng dần (task cũ nhất ở trên, mới nhất ở dưới). Mỗi entry dùng
> template ở mục "Cách thêm entry mới" phía cuối file. Nhớ thêm 1 dòng
> vào sơ đồ timeline bên dưới mỗi khi thêm entry mới.

## Timeline

```mermaid
timeline
    title Cinema Booking System — Tiến độ
    section 2026-07-28 — Nền tảng monolith
        Base project : Docker Compose (Postgres+Redis) : Flyway schema V1/V2 : Spring Boot skeleton
        Common exception handling : ApiError : ResourceNotFoundException : GlobalExceptionHandler
        CRUD Movie : entity/repo/mapper/dto : service/controller : test
    section 2026-07-29 — Nhân rộng pattern CRUD
        CRUD Genre : entity đơn giản : test
        CRUD Actor : entity + avatarUrl : test
        Tài liệu hoá : ROADMAP.md : PROGRESS_LOG.md
    section 2026-07-30 — Xác nhận build
        mvn test pass (28 test) : commit b1cc1c6
        Quan he Movie-Genre/Actor : MovieCast entity : test (31 test)
        Brand-Cinema-Room-SeatType-Seat : sinh so do ghe : test (77 test)
    section 2026-08-01 — Showtime
        CRUD Showtime : gan Movie + Room : test (88 test)
        API public cho User : movies/brands/cinemas/showtimes/seat-map : test (109 test)
        Seed data test (V3) + Postman collection : 52 request, chain qua bien
    section 2026-08-03 — Luong Booking
        CRUD-nho Booking : User entity toi thieu : create/get/history/cancel : check trung ghe app-level : test (126 test)
```

Xem chi tiết từng mốc ở các mục bên dưới (bấm vào tiêu đề để mở rộng).

---

<details>
<summary><strong>2026-07-28 — Base project: Docker Compose, Flyway schema, Spring Boot skeleton</strong> — nền tảng chạy được đầu tiên, có sẵn schema cho toàn Giai đoạn 1</summary>

**Mục đích:** có một project chạy được ngay từ ngày đầu (health check
sống), và có sẵn schema DB đầy đủ cho toàn bộ Giai đoạn 1 để không phải
sửa đi sửa lại migration khi thêm entity mới.

**Đã làm:**
- `docker-compose.yml`: Postgres 16 + Redis 7, có healthcheck, biến
  môi trường đọc từ `.env` (`DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`,
  `DB_PORT`, `REDIS_PORT`). Redis được setup từ bây giờ dù chưa dùng,
  để Giai đoạn 3 (seat-hold) không phải cấu hình lại.
- `src/main/resources/db/migration/V1__init_schema.sql`: tạo toàn bộ 14
  bảng cho Giai đoạn 1 — `users, brands, cinemas, rooms, seat_types,
  seats, movies, genres, movie_genres, actors, movie_actors, showtimes,
  bookings, booking_seats, payments`. Index cho các cột FK hay dùng để
  filter/join (`cinemas.brand_id`, `rooms.cinema_id`, `seats.room_id`,
  `showtimes.movie_id`, `showtimes(room_id, start_time)`,
  `bookings.user_id`, `bookings.showtime_id`, `booking_seats.booking_id`,
  `booking_seats.seat_id`, `payments.booking_id`).
- `src/main/resources/db/migration/V2__seed_reference_data.sql`: seed
  dữ liệu tham chiếu (chưa đọc chi tiết trong lần review này — xem file
  trực tiếp nếu cần).
- `src/main/resources/application.yml`: `hibernate.ddl-auto: validate`
  (Hibernate **không** được tự tạo/sửa schema — Flyway là nguồn sự thật
  duy nhất; nếu Entity lệch với DB, app báo lỗi ngay lúc start thay vì
  âm thầm sinh sai).
- `CinemaBookingApplication.java`, `controller/HealthController.java` —
  endpoint `/api/health` để xác nhận app chạy.
- `docs/ERD.md` — sơ đồ ERD dạng Mermaid, kèm giải thích quan hệ chính
  và ghi chú các ràng buộc **cố tình chưa làm** ở giai đoạn này (seat-hold
  bằng Redis thay vì bảng SQL; partial unique index chống bán trùng ghế
  — để dành Giai đoạn 2 khi business logic concurrency rõ ràng hơn).
- `pom.xml`: Spring Boot 3.3.4, Java 21, dependencies: `web`,
  `validation`, `data-jpa`, `postgresql` driver, `flyway-core` +
  `flyway-database-postgresql`, `lombok`, `springdoc-openapi` (Swagger
  UI để test API thay Postman), `devtools`.

**Test:** `HealthControllerTest` — test thuần, không cần Postgres/Redis
đang chạy.

**Trạng thái:** đã commit (`126e2fa first commit`, `f2e8d26 first commit
Base`).

</details>

<details>
<summary><strong>2026-07-28 — Common exception handling</strong> — 1 nơi xử lý lỗi thống nhất cho toàn bộ REST API</summary>

**Mục đích:** có 1 nơi xử lý lỗi thống nhất cho toàn bộ REST API, để
mọi module CRUD sau này (Movie, Genre, Actor, Cinema...) tái sử dụng —
không viết try/catch lặp lại trong từng controller.

**Đã làm:**
- `common/exception/ApiError.java` — record response lỗi chuẩn:
  `timestamp, status, message, fieldErrors` (map field → message lỗi
  validate). Có 2 static factory `ApiError.of(status, message)` và
  `ApiError.of(status, message, fieldErrors)`.
- `common/exception/ResourceNotFoundException.java` — `RuntimeException`
  để Service ném khi không tìm thấy entity theo id. Controller không
  cần biết gì về HTTP status.
- `common/exception/GlobalExceptionHandler.java` — `@RestControllerAdvice`
  toàn cục, bắt `ResourceNotFoundException` → 404, bắt
  `MethodArgumentNotValidException` (lỗi `@Valid` trên DTO) → 400 kèm
  `fieldErrors`.

**Quyết định kỹ thuật:** đặt exception handling ở `common/`, tách khỏi
package nghiệp vụ (`movie`, `genre`...) vì đây là hạ tầng dùng chung,
không thuộc riêng entity nào.

**Test:** chưa có test riêng cho `GlobalExceptionHandler` — được test
gián tiếp qua các test controller của từng module (VD:
`MovieControllerTest.findById_returns404WhenMissing`).

**Trạng thái:** chưa commit (untracked).

</details>

<details>
<summary><strong>2026-07-28 — CRUD Movie (<code>/api/admin/movies</code>)</strong> — entity nghiệp vụ đầu tiên, làm mẫu pattern CRUD chuẩn</summary>

**Mục đích:** entity nghiệp vụ đầu tiên của Giai đoạn 1, đồng thời làm
**mẫu pattern CRUD chuẩn** để copy sang các entity tĩnh khác (Genre,
Actor, và sau này Cinema, Room...).

**Đã làm:**
- `movie/Movie.java` — entity map bảng `movies`: `title` (not null),
  `description` (TEXT), `durationMin`, `language`, `releaseDate`,
  `posterUrl`, `trailerUrl`, `status` (enum, mặc định `COMING_SOON`),
  `viewCount` (mặc định 0, dùng cho bảng xếp hạng "phim nổi bật" ở màn
  hình 1 tab "Chọn phim").
- `movie/MovieStatus.java` — enum `NOW_SHOWING / COMING_SOON / ENDED`,
  map bằng `@Enumerated(EnumType.STRING)` để khớp `CHECK constraint`
  trên cột `movies.status` (lưu tên, không lưu ordinal).
- `movie/MovieRepository.java` — `JpaRepository<Movie, Long>` +
  derived query `findByStatus(MovieStatus)` (dùng cho API public sau
  này: danh sách phim đang chiếu/sắp chiếu).
- `movie/dto/MovieRequest.java` / `dto/MovieResponse.java` — **tách
  riêng DTO input/output**: `MovieRequest` không có `viewCount` vì đó
  là field server-controlled, form nhập từ Admin không được phép quyết
  định giá trị này. Validate bằng `@NotBlank`, `@NotNull`, `@Positive`
  với message tiếng Việt.
- `movie/MovieMapper.java` — mapper **viết tay**, không dùng MapStruct,
  để thấy rõ từng field đang map gì (quyết định có ghi chú lại: khi số
  field nhiều lên ở entity sau — Cinema, Room — có thể cân nhắc
  MapStruct).
- `movie/MovieService.java` — CRUD đầy đủ; `update()` **không gọi**
  `repository.save()` vì entity lấy từ `findById` trong transaction
  đang ở trạng thái managed — Hibernate tự UPDATE khi commit (dirty
  checking), gọi `save()` thêm là thừa.
- `movie/MovieController.java` — REST tại `/api/admin/movies` (namespace
  `/api/admin/...` để tách riêng với endpoint public `/api/movies` sẽ
  làm sau cho màn hình user xem phim). Chưa có Spring Security nên
  chưa có `@PreAuthorize` — sẽ thêm `hasRole("ADMIN")` khi làm JWT.

**Test:**
- `MovieServiceTest` (Mockito thuần, không `@SpringBootTest`, không cần
  Postgres chạy): create lưu đúng entity từ request, `findById`/`delete`
  ném `ResourceNotFoundException` khi không tồn tại, `update` áp giá trị
  mới đúng.
- `MovieControllerTest` (`@WebMvcTest` — chỉ load layer web, mock
  `MovieService` bằng `@MockBean`): create trả 201, validate lỗi trả
  400 kèm `fieldErrors`, `findById` không tồn tại trả 404, `delete` trả
  204.

**Trạng thái:** chưa commit (untracked).

</details>

<details>
<summary><strong>2026-07-29 — CRUD Genre (<code>/api/admin/genres</code>)</strong> — entity đơn giản, verify pattern Movie tái sử dụng tốt</summary>

**Mục đích:** entity thứ 2, đơn giản hơn Movie (chỉ `id` + `name`
unique), dùng để verify pattern CRUD ở Movie có tái sử dụng tốt không
trước khi nhân rộng cho các entity còn lại. Cần có trước khi nối quan
hệ `movie_genres`.

**Đã làm:** cấu trúc y hệt Movie —
`genre/Genre.java` (entity, `name varchar(50) unique not null`),
`genre/GenreRepository.java`, `genre/GenreMapper.java`,
`genre/dto/GenreRequest.java` (`@NotBlank`, `@Size(max = 50)`),
`genre/dto/GenreResponse.java`, `genre/GenreService.java` (CRUD +
`ResourceNotFoundException`), `genre/GenreController.java`
(`/api/admin/genres`).

**Quyết định kỹ thuật:** không thêm xử lý riêng cho lỗi trùng `name`
(DB có `UNIQUE`, nhưng chưa bắt `DataIntegrityViolationException`) —
để nguyên vì Movie cũng chưa xử lý case tương tự, giữ pattern nhất
quán; sẽ xử lý đồng loạt cho mọi entity khi cần (không làm riêng lẻ
từng module).

**Test:** `GenreServiceTest`, `GenreControllerTest` — cùng cấu trúc
test như Movie (Mockito cho Service, `@WebMvcTest` cho Controller).

**Trạng thái:** đã commit (`b1cc1c6`). `mvn test` pass (dùng Maven 3
bundle sẵn trong IntelliJ, `plugins/maven/lib/maven3/bin`, vì máy này
không có `mvn` cài rời trên PATH).

</details>

<details>
<summary><strong>2026-07-29 — CRUD Actor (<code>/api/admin/actors</code>)</strong> — tương tự Genre, thêm field avatarUrl</summary>

**Mục đích:** entity thứ 3, tương tự Genre nhưng có thêm field
`avatarUrl` (không unique). Cần có trước khi nối quan hệ
`movie_actors` (diễn viên + vai diễn trong phim, hiển thị ở màn hình
"Chi tiết phim").

**Đã làm:** cấu trúc y hệt Genre —
`actor/Actor.java` (entity, `name varchar(150) not null`,
`avatarUrl varchar(500)` nullable), `actor/ActorRepository.java`,
`actor/ActorMapper.java`, `actor/dto/ActorRequest.java` (`@NotBlank`,
`@Size(max = 150)` cho `name`; `avatarUrl` không validate vì optional),
`actor/dto/ActorResponse.java`, `actor/ActorService.java`,
`actor/ActorController.java` (`/api/admin/actors`).

**Test:** `ActorServiceTest`, `ActorControllerTest` — cùng cấu trúc
test như Movie/Genre.

**Trạng thái:** đã commit (`b1cc1c6`). `mvn test` pass.

</details>

<details>
<summary><strong>2026-07-29 — Tài liệu hoá roadmap và nhật ký tiến độ</strong> — ROADMAP.md + PROGRESS_LOG.md</summary>

**Mục đích:** có tài liệu để bất kỳ ai (kể cả người mới, hoặc chính tác
giả sau vài tuần quay lại) nhìn vào là hiểu tổng quan dự án đang ở đâu,
và tra được chi tiết từng task đã làm mà không phải đọc lại toàn bộ
code/git history.

**Đã làm:**
- `docs/ROADMAP.md` — tổng quan: mục tiêu, luồng UX 2 tab (Chọn
  phim / Chọn rạp) với chi tiết từng màn hình, tech stack và lý do dùng,
  5 giai đoạn của dự án (Monolith → Concurrency & Transaction → File &
  Payment → Microservice & Saga → Deploy & CI/CD) kèm checklist, và
  snapshot đang làm tới đâu.
- `docs/PROGRESS_LOG.md` (file này) — nhật ký chi tiết từng task, viết
  lại lịch sử từ base project đến Actor CRUD dựa trên code hiện có
  (git log chỉ có 2 commit gộp "first commit" nên không tách được mốc
  thời gian chi tiết hơn từ git — dùng timestamp file làm mốc tương đối).

**Trạng thái:** đã commit (`b1cc1c6`).

</details>

<details>
<summary><strong>2026-07-29 — Thêm timeline trực quan cho file này</strong> — Mermaid timeline + gói chi tiết vào khối gấp gọn</summary>

**Mục đích:** bản đầu của `PROGRESS_LOG.md` toàn text dài, khó lướt để
nắm tổng quan tiến độ. Cần 1 hình ảnh trực quan xem "đã làm gì theo thứ
tự thời gian" mà không phải đọc hết mọi đoạn văn.

**Đã làm:** thêm sơ đồ `mermaid timeline` ở đầu file (GitHub tự render
thành hình), nhóm theo ngày và giai đoạn; gói nội dung chi tiết của mỗi
entry cũ vào `<details><summary>` để mặc định thu gọn, chỉ hiện dòng
tóm tắt — bấm vào mới xem đầy đủ Mục đích/Đã làm/Test/Trạng thái.

**Trạng thái:** đã commit (`b1cc1c6`).

</details>

<details>
<summary><strong>2026-07-30 — Xác nhận build, commit lô CRUD đầu tiên</strong> — cài Maven qua bundle IntelliJ, `mvn test` pass, commit Movie/Genre/Actor/common</summary>

**Mục đích:** giải phóng điểm nghẽn "chưa xác nhận build" đã ghi ở các
entry trước, để có thể commit và yên tâm rẽ nhánh tiếp theo trên nền
code đã biết chắc chạy được.

**Đã làm:** phát hiện máy này không có `mvn` hay Maven Wrapper trên
PATH, nhưng có sẵn JDK 21 và Maven 3.9.11 bundle theo IntelliJ IDEA
(`.../plugins/maven/lib/maven3/bin`) — dùng trực tiếp thay vì cài thêm.
Chạy `mvn test`: 28 test, 0 fail/error, `BUILD SUCCESS`. Commit toàn bộ
`common/`, `actor/`, `genre/`, `movie/` (main + test) và 2 file doc
(`b1cc1c6`).

**Test:** không thêm test mới — chỉ xác nhận 28 test đã có (Movie,
Genre, Actor: Service + Controller, và `HealthControllerTest`) đều pass.

**Trạng thái:** đã commit (`b1cc1c6`).

</details>

<details>
<summary><strong>2026-07-30 — Nối quan hệ nhiều-nhiều Movie ↔ Genre / Movie ↔ Actor</strong> — MovieCast entity cho movie_actors, @ManyToMany cho movie_genres</summary>

**Mục đích:** màn hình "Chi tiết phim" (mục 3, `ROADMAP.md`) cần hiển thị
thể loại và dàn diễn viên/vai diễn — bảng `movie_genres`, `movie_actors`
đã có sẵn từ V1 nhưng chưa có liên kết ở tầng Java. Bắt buộc phải xong
trước khi làm Showtime/Booking.

**Đã làm:**
- `movie_genres` (thuần, không có cột riêng) → `Movie.genres` là
  `@ManyToMany` với `@JoinTable`, một chiều (không thêm `Genre.movies`
  vì chưa có màn hình nào cần).
- `movie_actors` (có thêm cột `role_name` gắn với từng cặp) → không
  dùng `@ManyToMany` được vì nó không mang thêm dữ liệu trên quan hệ.
  Tạo entity liên kết riêng: `movie/MovieCastId.java` (`@Embeddable`,
  khóa chính ghép `movieId` + `actorId`) và `movie/MovieCast.java`
  (`@EmbeddedId`, `@ManyToOne @MapsId` tới `Movie` và `Actor`, field
  `roleName`). `Movie.cast` là `@OneToMany(cascade = ALL, orphanRemoval
  = true)` để Service chỉ cần `clear()` + `addAll()` là Hibernate tự xoá
  dòng cũ/thêm dòng mới.
- `movie/dto/MovieCastRequest.java` (`actorId`, `roleName`),
  `movie/dto/MovieCastResponse.java` (phẳng hoá — `actorId, actorName,
  avatarUrl, roleName` — để FE không phải lồng `ActorResponse`).
  `MovieRequest` thêm `genreIds`, `cast` (không bắt buộc — phim mới có
  thể chưa gán gì). `MovieResponse` thêm `genres`, `cast`.
- `MovieMapper` giữ nguyên style hàm tĩnh thuần — không tự query DB,
  chỉ lắp ráp entity đã resolve sẵn (`toMovieCast`, `toResponse` mở
  rộng map thêm `genres`/`cast`).
- `MovieService`: tiêm thêm `GenreRepository`, `ActorRepository`;
  `resolveGenres`/`resolveCast` dùng `findAllById` (1 query, tránh N+1),
  ném `ResourceNotFoundException` nếu có id không tồn tại. `create()`/
  `update()` theo kiểu **replace-all**: mỗi lần gửi request là ghi đè
  toàn bộ genres/cast cũ.

**Quyết định kỹ thuật:**
- `create()` phải gọi `movieRepository.save(movie)` **trước** khi build
  `MovieCast` — `movies.id` dùng chiến lược `IDENTITY`, chỉ có giá trị
  sau khi insert, mà `MovieCastId` cần `movie.getId()` để dựng khóa
  chính ghép.
- Cố tình không thêm chiều ngược (`Genre.movies`, `Actor.movies`) và
  không tối ưu N+1 khi `findAll()` load quan hệ của nhiều phim cùng lúc
  (`@EntityGraph`/fetch join) — chưa có nhu cầu thật, để dành khi có vấn
  đề hiệu năng cụ thể.

**Test:** `MovieServiceTest` thêm mock `GenreRepository`,
`ActorRepository`; test case mới `create_resolvesGenresAndCast`,
`create_throwsWhenGenreIdNotFound`, `update_replacesExistingCastAndGenres`.
`MovieControllerTest` cập nhật request/response mẫu cho có `genreIds`,
`cast`. Tổng `mvn test`: 31 test, 0 fail/error, `BUILD SUCCESS`.

**Trạng thái:** đã commit (`d7f37b4`).

</details>

<details>
<summary><strong>2026-07-30 — Nhánh Brand → Cinema → Room → SeatType → Seat</strong> — CRUD chuẩn cho 4 entity + API sinh sơ đồ ghế cho Room</summary>

**Mục đích:** phục vụ tab "Chọn rạp" (màn hình 1: chọn hãng/rạp) và sơ
đồ ghế sau này (màn hình 4, cả 2 tab). Bắt buộc phải xong trước khi làm
Showtime/Booking.

**Đã làm:**
- `brand/` — CRUD chuẩn y hệt `genre/` (không có entity cha): `name`,
  `logoUrl`.
- `cinema/` — CRUD thuộc 1 `Brand`. `Cinema` có `@ManyToOne Brand`
  (không back-ref `Brand.cinemas`). `CinemaService` tiêm thêm
  `BrandRepository`, resolve `brandId` → `ResourceNotFoundException`
  nếu không có (đúng cách `MovieService.resolveGenres` đã làm).
  `CinemaResponse` phẳng hoá `brandId` + `brandName` thay vì lồng
  `BrandResponse`.
- `room/` — CRUD thuộc 1 `Cinema`, cấu trúc y hệt `cinema/`. `roomType`
  cố tình để `String` tự do (mặc định `"2D"`) thay vì enum — schema
  `rooms.room_type` không có `CHECK` constraint như `movies.status`,
  nên không ép về 1 tập giá trị cố định ở tầng Java.
- `seattype/` — CRUD chuẩn y hệt `genre/`: `name` (unique),
  `priceMultiplier` (`BigDecimal`, khớp `NUMERIC(4,2)`).
- `seat/` — **không phải CRUD chuẩn**: ghế luôn sinh hàng loạt theo sơ
  đồ phòng, không có form tạo/sửa từng ghế. API:
  `POST /api/admin/rooms/{roomId}/seats` (`SeatLayoutRequest` — danh
  sách hàng, mỗi hàng gồm `rowLabel`, `columnCount`, `seatTypeId`) ghi
  đè toàn bộ ghế cũ của phòng: `SeatService.generateLayout` validate
  Room + mọi `seatTypeId` tồn tại, gọi `seatRepository.deleteByRoomId`
  rồi `flush()` (bắt buộc — nếu không flush, DELETE có thể chưa xuống
  DB trước khi INSERT ghế mới, vi phạm `UNIQUE(room_id, row_label,
  col_number)` khi sinh lại đúng layout cũ), rồi sinh ghế mới đánh số
  cột `1..columnCount` cho mỗi hàng.
  `GET /api/admin/rooms/{roomId}/seats` trả sơ đồ hiện tại
  (`findByRoomIdOrderByRowLabelAscColNumberAsc`).

**Quyết định kỹ thuật:**
- Không thêm collection ngược (`Brand.cinemas`, `Cinema.rooms`,
  `Room.seats`) — chưa màn hình nào cần duyệt theo chiều đó, giữ nhất
  quán với quyết định đã đưa ra ở Movie↔Genre/Actor.
- Không tự bắt `DataIntegrityViolationException` khi 2 hàng trong 1
  request trùng `rowLabel` — để DB tự chặn bằng
  `UNIQUE(room_id, row_label, col_number)`, giữ nhất quán với cách
  Genre/Movie hiện tại chưa xử lý case tương tự.
- Seat không có sửa/xoá từng ghế lẻ, chỉ generate cả layout — đúng đúng
  phạm vi "sinh Seat theo sơ đồ phòng" trong checklist, tránh code thừa
  cho use case chưa xuất hiện.

**Test:** mỗi entity CRUD chuẩn (Brand, Cinema, Room, SeatType) có
`XxxServiceTest` (Mockito) + `XxxControllerTest` (`@WebMvcTest`), đúng
bộ test case như Movie/Genre. `SeatServiceTest` verify sinh đúng số ghế
theo `columnCount`, gọi `deleteByRoomId` trước khi insert, ném
`ResourceNotFoundException` khi thiếu `roomId`/`seatTypeId`.
`SeatControllerTest` verify `POST`/`GET` cơ bản. Tổng `mvn test`: 77
test, 0 fail/error, `BUILD SUCCESS`.

**Trạng thái:** đã commit (`eec952f`).

</details>

<details>
<summary><strong>2026-08-01 — CRUD Showtime (<code>/api/admin/showtimes</code>)</strong> — gắn Movie + Room + khung giờ + giá vé cơ bản, hoàn tất phần entity của Giai đoạn 1</summary>

**Mục đích:** mục cuối cùng còn thiếu trước khi làm API public cho User
và luồng Booking — suất chiếu là điểm nối giữa `Movie` và `Room`, cần có
trước khi sinh sơ đồ ghế theo suất chiếu hay tạo booking.

**Đã làm:** cấu trúc CRUD y hệt `room/` (resolve 2 FK thay vì 1) —
`showtime/Showtime.java` (entity: `@ManyToOne Movie`, `@ManyToOne Room`,
`startTime`/`endTime` kiểu `OffsetDateTime` khớp cột `TIMESTAMPTZ`,
`basePrice` kiểu `BigDecimal` khớp `NUMERIC(10,2)`),
`showtime/ShowtimeRepository.java`,
`showtime/dto/ShowtimeRequest.java` (`movieId`, `roomId`, `startTime`,
`endTime`, `basePrice` đều `@NotNull`; `basePrice` thêm `@Positive`),
`showtime/dto/ShowtimeResponse.java` (phẳng hoá `movieTitle`,
`roomName` — đúng cách `RoomResponse` phẳng hoá `cinemaName`),
`showtime/ShowtimeMapper.java`, `showtime/ShowtimeService.java`
(`getMovieOrThrow`/`getRoomOrThrow` giống `RoomService.getCinemaOrThrow`),
`showtime/ShowtimeController.java` (`/api/admin/showtimes`).

**Quyết định kỹ thuật:** không thêm validate chéo `endTime` phải sau
`startTime` — không có tiền lệ validate nghiệp vụ kiểu này ở các entity
khác (VD: Genre/Movie cũng chưa bắt trùng `name`), giữ pattern nhất
quán; để dành xử lý đồng loạt khi có nhu cầu thật.

**Test:** `ShowtimeServiceTest` (Mockito, mock cả `MovieRepository` và
`RoomRepository`), `ShowtimeControllerTest` (`@WebMvcTest`) — cùng bộ
test case như Room (create thành công, ném `ResourceNotFoundException`
khi thiếu `movieId`/`roomId`, `findById` 404, update, delete). Tổng
`mvn test`: 88 test, 0 fail/error, `BUILD SUCCESS` (dùng Maven bundle
theo IntelliJ Community 2025.2.6,
`plugins/maven/lib/maven3/bin`).

**Trạng thái:** đã commit (`1e8119e`).

</details>

<details>
<summary><strong>2026-08-01 — API public cho User (<code>/api/movies</code>, <code>/api/brands</code>, <code>/api/cinemas/{cinemaId}/showtimes</code>, <code>/api/showtimes/{id}/seats</code>)</strong> — endpoint không cần quyền ADMIN, phục vụ luồng UX ở màn hình User</summary>

**Mục đích:** hoàn tất Giai đoạn 1 phần "đọc dữ liệu" — trang chủ phim,
chi tiết phim, chọn hãng/rạp, chọn suất, chọn ghế (mục 3, `ROADMAP.md`)
— trước khi vào luồng Booking. Tách hẳn khỏi `/api/admin/...` vì sau
này thêm JWT, các endpoint dưới `/api/admin` sẽ yêu cầu `hasRole("ADMIN")`
còn nhóm này phải mở cho mọi người.

**Đã làm:**
- `movie/MoviePublicController.java` (`/api/movies`): `GET` (query
  `status`/`q`, forward vào `MovieService.search` mới — service tự
  quyết định dùng `findByTitleContainingIgnoreCase`, `findByStatus`,
  hay `findAll` tuỳ tham số nào có), `GET /featured?limit=` (top theo
  `viewCount` desc, `MovieService.findFeatured`), `GET /{id}` (tái dùng
  nguyên `MovieService.findById` đã có, không đổi gì).
- `brand/BrandPublicController.java` (`/api/brands`): `GET`, tái dùng
  `BrandService.findAll()` có sẵn, không thêm method mới.
- `cinema/CinemaPublicController.java`
  (`/api/brands/{brandId}/cinemas`): `GET`, method mới
  `CinemaService.findByBrand(brandId)` validate brand tồn tại (tái dùng
  `getBrandOrThrow` đã có) rồi gọi `CinemaRepository.findByBrandId`
  (derived query mới).
- `showtime/ShowtimePublicController.java`
  (`/api/cinemas/{cinemaId}/showtimes`): `GET` với `date` bắt buộc,
  `movieId` optional. `ShowtimeService.findByCinemaAndDate` convert
  `LocalDate` → khoảng `[00:00, +1 ngày)` theo `ZoneId.of("Asia/Ho_Chi_Minh")`
  cố định (rạp thuộc VN, không phụ thuộc timezone server), rồi gọi 1
  trong 2 derived query mới của `ShowtimeRepository`
  (`findByRoom_Cinema_IdAndStartTimeBetween` hoặc thêm `Movie_Id` khi có
  `movieId`).
- `showtime/ShowtimeSeatController.java` (`/api/showtimes`): `GET
  /{id}/seats` trả `ShowtimeSeatMapResponse` (DTO mới, gồm thông tin
  suất chiếu + `List<ShowtimeSeatResponse>`). `ShowtimeService.getSeatMap`
  lấy showtime, dùng lại `SeatRepository.findByRoomIdOrderByRowLabelAscColNumberAsc`
  đã có (từ tính năng sinh sơ đồ ghế), tính giá từng ghế = `basePrice *
  seatType.priceMultiplier` trong `ShowtimeMapper.toSeatMapResponse`
  (method mới).

**Quyết định kỹ thuật:**
- Không tạo Service/Controller mới nếu response shape hiện có
  (`MovieResponse`, `BrandResponse`, `CinemaResponse`, `ShowtimeResponse`)
  đã đủ dùng — chỉ có sơ đồ ghế mới cần DTO riêng.
- `Showtime` có 2 base path public khác nhau
  (`/api/cinemas/{cinemaId}/showtimes` và `/api/showtimes/{id}/seats`) nên
  tách thành 2 controller (`ShowtimePublicController`,
  `ShowtimeSeatController`) thay vì gộp path tuyệt đối vào 1 class — giữ
  đúng convention "1 controller = 1 `@RequestMapping` class-level" đang
  dùng xuyên suốt dự án (Spring nối path method vào path class chứ
  không override).
- Package của controller mới đặt theo **resource trả về**, không theo
  URL cha — đúng tiền lệ `SeatController` (`seat/` package dù URL thuộc
  `/api/admin/rooms/{roomId}/seats`, nằm dưới `room`). Áp dụng: rạp-theo-hãng
  vẫn là `cinema/CinemaPublicController` dù URL bắt đầu bằng
  `/api/brands/...`.
- Sơ đồ ghế theo suất chiếu **chưa có trạng thái còn trống/đã đặt** — chỉ
  layout + giá. `Booking`/`BookingSeat` chưa có ở tầng Java (dù bảng đã
  có sẵn từ V1), việc gắn trạng thái ghế thuộc "Luồng Booking", mục tiếp
  theo trong checklist.
- Không ép `date` mặc định "hôm nay" ở backend cho
  `/api/cinemas/{cinemaId}/showtimes` — để FE tự truyền theo UX doc, giữ
  API tường minh. Không chặn `limit` âm ở `/api/movies/featured` — nhất
  quán với mức validate tối thiểu đã áp dụng cho toàn bộ CRUD trước đó.

**Test:** 5 file test controller mới (`MoviePublicControllerTest`,
`BrandPublicControllerTest`, `CinemaPublicControllerTest`,
`ShowtimePublicControllerTest`, `ShowtimeSeatControllerTest`, đều
`@WebMvcTest` + Mockito `@MockBean` như các controller khác). Thêm case
vào `MovieServiceTest` (`search` theo `q`/theo `status`/không lọc gì,
`findFeatured`), `CinemaServiceTest` (`findByBrand` thành công + brand
không tồn tại), `ShowtimeServiceTest` (`findByCinemaAndDate` có/không
`movieId` + cinema không tồn tại, `getSeatMap` tính đúng giá + showtime
không tồn tại — thêm mock `CinemaRepository`, `SeatRepository`). Tổng
`mvn test`: 109 test, 0 fail/error, `BUILD SUCCESS`.

**Trạng thái:** đã commit (`1e8119e`).

</details>

<details>
<summary><strong>2026-08-01 — Seed du lieu test (V3) + Postman collection</strong> — cong cu ho tro test thu cong, khong thuoc checklist Giai doan 1</summary>

**Mục đích:** can du lieu that de test toan bo API (admin + public) bang
Postman thay vi phai tu tay tao qua Swagger tung buoc. Khong thuoc 1 muc
checklist cu the trong `ROADMAP.md` - la cong cu ho tro phat trien.

**Đã làm:**
- `src/main/resources/db/migration/V3__seed_test_data.sql` - migration
  Flyway moi (**khong sua V2** - V2 da duoc apply va ghi checksum vao
  `flyway_schema_history` cua DB local dang chay, sua se lam Flyway bao
  loi checksum mismatch lan chay tiep theo). Seed: 2 brand (CGV, BHD),
  moi brand 1 rap + 1 phong (40 ghe/phong, sinh bang `generate_series`
  giong cach `SeatService.generateLayout` sinh o tang ung dung), 6 the
  loai, 6 dien vien, 6 phim (3 NOW_SHOWING, 2 COMING_SOON, 1 ENDED, co
  `movie_genres`/`movie_actors`), 6 suat chieu (hom nay + ngay mai, gio
  VN co dinh qua `AT TIME ZONE 'Asia/Ho_Chi_Minh'` giong cach
  `ShowtimeService` dang lam). Insert dung subquery theo ten (natural
  key) thay vi hardcode id.
- `postman/cinema-booking-service.postman_collection.json` - collection
  Postman v2.1, 15 folder / 52 request, gom toan bo API: `Health`,
  `Admin - {Genre, Actor, Brand, Cinema, Room, SeatType, Seat (layout),
  Movie, Showtime}` (CRUD day du), `Public - {Movie, Brand, Cinema,
  Showtime, Seat Map}` (chi doc).

**Quyết định kỹ thuật:**
- Phat hien khi test that: Postgres IDENTITY sequence **khong roll back**
  khi 1 transaction chua no bi rollback (kieu ca 1 lan migration that bai
  giua chung o lan chay dau, va 1 lan dry-run qua `psql BEGIN;...;ROLLBACK;`
  de kiem tra cu phap) - nen id thuc te sau khi seed **khong dam bao bat
  dau tu 1** (thuc te: brand id 5/6, movie id 8-13...). Vi vay Postman
  collection **khong hardcode id nao ca** - moi request `List ...` (dau
  moi folder) co Tests script luu id dau tien vao 1 collection variable
  (VD `{{movieId}}`, `{{cinemaId}}`), cac request sau dung lai bien do.
  Day cung la thuc hanh chuan cua Postman cho collection co quan he cha-con.
- Moi folder Admin CRUD tu tao 1 ban ghi demo rieng (`Create` luu
  `{{createdXxxId}}`) de test Update/Delete, **khong dung/xoa du lieu
  seed** - vi vay Public folder luon co du lieu de demo ma khong can seed
  lai. Rieng "Generate Seat Layout" dung `{{createdRoomId}}` (phong demo)
  chu khong dung `{{roomId}}` (phong seed) vi request nay ghi de toan bo
  ghe cua phong - dung nham phong seed se pha layout 40 ghe dang dung o
  Public - Seat Map.
- Collection co pre-request script o muc collection tu sinh
  `{{today}}` (ngay hien tai, dung cho query `date` cua
  `/api/cinemas/{cinemaId}/showtimes` va body tao Showtime) - khong can
  nguoi dung tu dien tay.

**Test:** khong co Newman/CLI Postman tren may (chi co the validate cu
phap JSON qua `node -e "require(...)"`). Da smoke-test thu cong qua
`curl` toan bo chuoi phu thuoc quan trong cua collection (Genre CRUD day
du; Cinema→Room→SeatType→Generate Seat Layout; Movie voi
`genreIds`/`cast`; Showtime voi `{{today}}`) truc tiep tren app dang
chay that (`mvn spring-boot:run`, Postgres tu docker-compose) - deu tra
dung status code va du lieu ky vong. Da xoa cac ban ghi demo tao ra
trong luc smoke-test, xac nhan lai row count khop dung du lieu seed (2
brand, 2 rap, 2 phong, 80 ghe, 6 phim, 6 the loai, 6 dien vien, 6 suat
chieu) - khong dung toi `mvn test` (khong co test moi o muc Java, day la
migration SQL + file JSON tinh).

**Trạng thái:** đã commit (`1e8119e`).

</details>

<details>
<summary><strong>2026-08-03 — Luồng Booking (<code>/api/bookings</code>)</strong> — tạo booking từ ghế đã chọn, tính tiền, quản lý trạng thái PENDING/PAID/CANCELLED/EXPIRED</summary>

**Mục đích:** mục tiếp theo trong checklist Giai đoạn 1 sau khi toàn bộ
entity tĩnh + Showtime + API public đọc dữ liệu đã xong — nối
`Showtime`/`Seat` với `Payment giả lập` (bước kế tiếp). Đây cũng là lần
đầu tiên `bookings`/`booking_seats`/`users` (có sẵn từ V1) có entity
Java.

**Đã làm:**
- `user/User.java` + `user/UserRepository.java` — entity **tối thiểu**
  map bảng `users` (`name, email, passwordHash, provider, providerId,
  role, createdAt`), chỉ đủ để `Booking` có `@ManyToOne User` hợp lệ.
  Cố tình **không** làm Service/Controller/DTO — CRUD/JWT thật sẽ làm
  ở mục checklist riêng "Spring Security + JWT" (sau Booking).
- `booking/BookingStatus.java` — enum `PENDING/PAID/CANCELLED/EXPIRED`,
  map `@Enumerated(EnumType.STRING)` giống `MovieStatus`.
- `booking/Booking.java` — `@ManyToOne User`, `@ManyToOne Showtime`,
  `status` (mặc định `PENDING`), `totalPrice`, `createdAt` (set =
  `OffsetDateTime.now()` ở tầng Java lúc tạo, không dựa DB default để
  khỏi phải reload entity sau insert). `seats` —
  `@OneToMany(mappedBy = "booking", cascade = ALL, orphanRemoval =
  true)`, đúng pattern `Movie.cast`.
- `booking/BookingSeat.java` — map `booking_seats`, có `id` riêng
  (khác `MovieCast` — bảng này đã có sẵn cột `id` IDENTITY nên không
  cần `@EmbeddedId`). `price` là **snapshot giá tại thời điểm đặt**
  (đúng ghi chú đã có trong `docs/ERD.md`), không tính lại từ
  `basePrice` khi truy vấn sau này.
- `booking/BookingRepository.java` (+ `findByUserIdOrderByCreatedAtDesc`
  cho lịch sử đặt vé), `booking/BookingSeatRepository.java` — 1 JPQL
  query `findBookedSeatIds` join thẳng `booking.showtime`/
  `booking.status` để check trùng ghế mà không load nguyên `Booking`
  graph.
- `booking/dto/{BookingRequest, BookingSeatResponse, BookingResponse}` —
  `BookingRequest` nhận `userId` trực tiếp trong body (chưa có JWT nên
  chưa có "current user"), `seatIds` (`@NotEmpty`). `BookingResponse`
  phẳng hoá `movieTitle`/`roomName`/`startTime` từ showtime, đúng style
  `ShowtimeResponse`.
- `booking/BookingMapper.java` — tĩnh, tái dùng đúng công thức tính giá
  ghế đã có ở `ShowtimeMapper.toSeatMapResponse`
  (`basePrice x seatType.priceMultiplier`).
- `booking/BookingService.java`:
  - `create`: resolve `User`/`Showtime` → resolve + validate từng
    `seatId` thuộc đúng phòng của showtime (`ResourceNotFoundException`
    nếu không) → check trùng ghế qua `findBookedSeatIds` với trạng
    thái `[PENDING, PAID]` (ném `BookingConflictException` nếu trùng)
    → tính giá từng ghế + tổng tiền → save (cascade lo `booking_seats`).
  - `findById`, `findByUser` (validate user tồn tại), `cancel` (chỉ
    cho phép khi `status == PENDING`, không gọi `save()` — managed
    entity, dirty checking, đúng quyết định đã áp dụng ở
    `MovieService.update`).
- `booking/BookingController.java` (`/api/bookings`): `POST` (201),
  `GET /{id}`, `GET ?userId=` (lịch sử — dùng query param thay vì path
  riêng, giống cách `MoviePublicController` dùng `status`/`q`),
  `PATCH /{id}/cancel`.
- `common/exception/BookingConflictException.java` — exception mới,
  thêm handler trong `GlobalExceptionHandler` → `409 CONFLICT`.

**Quyết định kỹ thuật:**
- Check trùng ghế làm ở **tầng Service** (app-level), không phải DB
  constraint — bảng `booking_seats` chưa có partial unique index (để
  dành Giai đoạn 2 khi làm lock/Redis seat-hold, xem
  `docs/ERD.md` mục "Deferred constraints"). Cách này chặn được trường
  hợp thường (không có 2 request đồng thời) nhưng **không** chống được
  race condition thật — chấp nhận được vì đó là mục tiêu học riêng của
  Giai đoạn 2, đã xác nhận với user trước khi code (AskUserQuestion).
- Không tách admin/public cho `/api/bookings` — chưa có JWT nên chưa
  phân biệt được người gọi API; sẽ khoá quyền khi làm Spring Security.
- Không tự động chuyển `EXPIRED` (cần scheduled job dọn booking
  `PENDING` quá hạn) — ngoài phạm vi checklist "Luồng Booking", để dành
  khi có nhu cầu thật (có thể gộp cùng lúc với Giai đoạn 2).

**Test:** `BookingServiceTest` (Mockito, mock đủ 5 repository liên
quan): tạo booking tính đúng giá/tổng tiền, ném lỗi khi thiếu
`userId`/`showtimeId`, ném lỗi khi ghế không thuộc phòng của showtime,
ném `BookingConflictException` khi ghế đã bị đặt, `findById` 404,
`findByUser` (user không tồn tại → 404, có kết quả), `cancel` thành
công khi `PENDING` + ném lỗi khi không phải `PENDING`.
`BookingControllerTest` (`@WebMvcTest`): status code cho từng endpoint
(`201/200/404/409`, validate `400` khi `seatIds` rỗng). Tổng `mvn test`:
126 test, 0 fail/error, `BUILD SUCCESS`.

**Trạng thái:** đã commit (`1e8119e`).

</details>

---

## Cách thêm entry mới

Copy template dưới đây, điền vào cuối file này (không chèn giữa các
entry cũ), rồi:
1. Thêm 1 dòng tương ứng vào sơ đồ `mermaid timeline` ở đầu file (theo
   đúng ngày, tạo `section` mới nếu sang giai đoạn khác).
2. Cập nhật checklist tương ứng trong `ROADMAP.md` nếu task này hoàn
   thành 1 mục ở đó.

```markdown
<details>
<summary><strong>YYYY-MM-DD — Tên task ngắn gọn</strong> — 1 dòng tóm tắt hiện ra khi thu gọn</summary>

**Mục đích:** tại sao làm task này, nó phục vụ tính năng/màn hình nào.

**Đã làm:** liệt kê file/module đã tạo hoặc sửa, mỗi cái 1 dòng, nói rõ
field/behavior quan trọng — không chỉ liệt kê tên file.

**Quyết định kỹ thuật:** (nếu có) lựa chọn nào đã cân nhắc, tại sao chọn
cách này chứ không phải cách khác. Bỏ mục này nếu task không có quyết
định đáng nói.

**Test:** test nào đã viết, cover case gì.

**Trạng thái:** đã commit (kèm hash) / chưa commit / đã chạy `mvn test`
pass hay chưa.

</details>
```
