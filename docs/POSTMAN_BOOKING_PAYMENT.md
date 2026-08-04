# Chi tiết API — folder "Booking" và "Payment" (Postman)

File này giải thích **từng request** trong 2 folder `Booking` và `Payment`
của `postman/cinema-booking-service.postman_collection.json`: method, path,
request/response mẫu, mã lỗi, và biến collection mà request đó đọc/ghi.
Bối cảnh tổng thể (mục tiêu dự án, giai đoạn) xem
[`ROADMAP.md`](./ROADMAP.md); chi tiết quyết định kỹ thuật khi implement
xem [`PROGRESS_LOG.md`](./PROGRESS_LOG.md).

## Điều kiện tiên quyết

Trước khi chạy 2 folder này, cần có sẵn 3 biến collection:

| Biến | Cách lấy |
|---|---|
| `{{userId}}` | Tra thủ công qua psql (`users` chưa có API) — xem mục "Chuẩn bị" |
| `{{brandId}}`, `{{cinemaId}}`, `{{showtimeId}}` | Chạy lần lượt `Public - Brand → List Brands` → `Public - Cinema → Cinemas By Brand` → `Public - Showtime → Showtimes By Cinema And Date` (mỗi request tự lưu id đầu tiên vào biến qua Tests script) |

```bash
docker exec cinema-postgres psql -U cinema_user -d cinema_booking \
  -tAc "SELECT id FROM users WHERE email='nguyen.van.a@example.com';"
```
→ set giá trị trả về vào collection variable `userId` (tab Variables → CURRENT VALUE → Save).

---

## Folder "Booking" — `/api/bookings`

Mô phỏng luồng đặt vé: chọn ghế → tạo booking → xem/huỷ. Đúng theo thứ tự
đánh số trong tên request.

### 1. Seat Map (Pick Seats For Booking)

| | |
|---|---|
| Method | `GET` |
| URL | `{{baseUrl}}/api/showtimes/{{showtimeId}}/seats` |
| Auth | Không |
| Body | Không |

Đây thực chất là API **public đọc dữ liệu** (đã có từ trước, không thuộc
`payment`/`booking`), được đặt trong folder Booking vì user cần xem sơ đồ
ghế trước khi đặt. Response mẫu:

```json
{
  "showtimeId": 7,
  "movieTitle": "Avengers: Doomsday",
  "roomName": "Phong 1",
  "startTime": "2026-08-01T10:00:00+07:00",
  "seats": [
    { "id": 101, "rowLabel": "A", "colNumber": 1, "seatType": "Standard", "price": 90000.00 },
    { "id": 102, "rowLabel": "A", "colNumber": 2, "seatType": "Standard", "price": 90000.00 }
  ]
}
```

**Tests script** lưu 2 ghế đầu tiên vào `{{bookingSeatId1}}` / `{{bookingSeatId2}}`
để request "Create Booking" dùng.

> Sơ đồ ghế **chưa có trạng thái còn trống/đã đặt** — chỉ layout + giá.
> Việc kiểm tra ghế đã bị đặt hay chưa nằm ở tầng `POST /api/bookings`
> (Giai đoạn 2 mới thêm hiển thị trạng thái ghế real-time qua Redis).

---

### 2. Create Booking

| | |
|---|---|
| Method | `POST` |
| URL | `{{baseUrl}}/api/bookings` |
| Header | `Content-Type: application/json` |

**Request body:**
```json
{
    "userId": {{userId}},
    "showtimeId": {{showtimeId}},
    "seatIds": [{{bookingSeatId1}}, {{bookingSeatId2}}]
}
```

**Xử lý ở server** (`BookingService.create`):
1. Validate `userId`, `showtimeId` tồn tại → `404` nếu không
2. Validate từng `seatId` tồn tại **và thuộc đúng phòng chiếu** của showtime → `404` nếu sai
3. Check ghế đã bị đặt bởi booking khác đang `PENDING`/`PAID` chưa → `409` nếu trùng
4. Tính giá từng ghế = `basePrice × seatType.priceMultiplier`, cộng thành `totalPrice`
5. Lưu booking, status mặc định `PENDING`

**Response `201`:**
```json
{
    "id": 1,
    "userId": 1,
    "showtimeId": 7,
    "movieTitle": "Avengers: Doomsday",
    "roomName": "Phong 1",
    "startTime": "2026-08-01T10:00:00+07:00",
    "status": "PENDING",
    "totalPrice": 180000.00,
    "createdAt": "2026-08-04T09:00:00+07:00",
    "ticketCode": null,
    "seats": [
        { "seatId": 101, "rowLabel": "A", "colNumber": 1, "price": 90000.00 },
        { "seatId": 102, "rowLabel": "A", "colNumber": 2, "price": 90000.00 }
    ]
}
```

**Tests script** lưu `id` vào `{{bookingId}}`.

`ticketCode` luôn là `null` ở bước này — chỉ có giá trị sau khi thanh toán
thành công (xem folder Payment).

---

### 3. Create Booking - Seat Already Booked (409)

Y hệt request #2 (dùng lại đúng `{{bookingSeatId1}}`/`{{bookingSeatId2}}`
vừa đặt thành công) để minh hoạ chống trùng ghế.

**Response `409`:**
```json
{
    "timestamp": "2026-08-04T09:01:00Z",
    "status": 409,
    "message": "Ghe da duoc dat: [101, 102]",
    "fieldErrors": null
}
```

> ⚠️ Check này chạy ở **tầng application** (query DB rồi so sánh trong Java),
> không phải constraint DB — chỉ chặn được trường hợp thường (2 request nối
> tiếp nhau), **không chống được race condition thật** khi 2 request đến
> đồng thời. Đây là mục tiêu học riêng của Giai đoạn 2 (concurrency & lock).

---

### 4. Get Booking By Id

| | |
|---|---|
| Method | `GET` |
| URL | `{{baseUrl}}/api/bookings/{{bookingId}}` |

Trả về đúng shape `BookingResponse` như request #2. `404` nếu `bookingId`
không tồn tại.

---

### 5. Booking History By User

| | |
|---|---|
| Method | `GET` |
| URL | `{{baseUrl}}/api/bookings?userId={{userId}}` |

Trả về `List<BookingResponse>` của user đó, mới nhất trước
(`ORDER BY created_at DESC`). `404` nếu `userId` không tồn tại.

---

### 6. Cancel Booking

| | |
|---|---|
| Method | `PATCH` |
| URL | `{{baseUrl}}/api/bookings/{{bookingId}}/cancel` |
| Body | Không |

Chỉ huỷ được khi `status == PENDING` → chuyển thành `CANCELLED`
(`200`, trả về `BookingResponse` đã cập nhật).

---

### 7. Cancel Booking - Already Cancelled (409)

Y hệt request #6, gọi lại lần nữa trên booking **đã** `CANCELLED`.

**Response `409`:**
```json
{
    "status": 409,
    "message": "Chi huy duoc booking dang o trang thai PENDING"
}
```

---

## Folder "Payment" — `/api/payments` + `/api/bookings/{id}/ticket`

Mô phỏng thanh toán giả lập (bypass — không gọi gateway thật) và xem vé/QR
sau khi thanh toán. Dùng **booking riêng** (ghế thứ 3/4 trong sơ đồ, khác
với ghế folder Booking đã dùng) để 2 folder không tranh ghế khi chạy chung
1 lượt.

### 1. Seat Map (Pick Seats For Payment Booking)

Giống hệt request #1 của folder Booking, nhưng **Tests script lấy ghế thứ
3 và thứ 4** (`seats[2]`, `seats[3]`) thay vì ghế đầu tiên, lưu vào
`{{paymentSeatId1}}` / `{{paymentSeatId2}}`.

---

### 2. Create Booking For Payment

Giống request #2 của folder Booking, nhưng dùng `{{paymentSeatId1/2}}`.
Tests script lưu `id` vào `{{paymentBookingId}}` (biến riêng, không đụng
`{{bookingId}}` của folder Booking).

---

### 3. Get Ticket - Not Paid Yet (409)

| | |
|---|---|
| Method | `GET` |
| URL | `{{baseUrl}}/api/bookings/{{paymentBookingId}}/ticket` |

Gọi **trước khi thanh toán** để minh hoạ: booking đang `PENDING` thì chưa
có vé.

**Response `409`:**
```json
{
    "status": 409,
    "message": "Booking chua thanh toan, chua co ve"
}
```

---

### 4. Pay Booking

| | |
|---|---|
| Method | `POST` |
| URL | `{{baseUrl}}/api/payments` |
| Header | `Content-Type: application/json` |

**Pre-request script** (chạy trước khi gửi) tự sinh 1 `idempotencyKey`
mới, lưu vào `{{paymentIdempotencyKey}}`:
```js
pm.collectionVariables.set('paymentIdempotencyKey',
    'idem-' + Date.now() + '-' + Math.floor(Math.random() * 100000));
```

**Request body:**
```json
{
    "bookingId": {{paymentBookingId}},
    "idempotencyKey": "{{paymentIdempotencyKey}}"
}
```

**Xử lý ở server** (`PaymentService.pay`):
1. Tra `idempotencyKey` trong DB — chưa tồn tại (lần đầu) nên đi tiếp
2. Validate `booking` tồn tại (`404` nếu không) và đang `PENDING` (`409`
   nếu không — VD đã `PAID`/`CANCELLED`)
3. Tạo `Payment` mới — **luôn `SUCCESS` ngay lập tức** (bypass, không gọi
   cổng thanh toán thật, đúng tinh thần "giả lập" của Giai đoạn 1)
4. Cập nhật `booking.status = PAID`, sinh `ticketCode` mới cho booking
5. Sinh QR code **thật** (ảnh PNG, thư viện ZXing) từ `ticketCode`, encode
   base64

**Response `201`:**
```json
{
    "id": 1,
    "bookingId": 3,
    "status": "SUCCESS",
    "method": "FAKE_GATEWAY",
    "transactionRef": "FAKE-3f2a1b0c-...",
    "idempotencyKey": "idem-1754297000-12345",
    "createdAt": "2026-08-04T09:05:00+07:00",
    "ticketCode": "CB-235VMMZ6",
    "qrCodeBase64": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA..."
}
```

**Tests script** kiểm tra `status == 'SUCCESS'` và `ticketCode`/
`qrCodeBase64` có giá trị hợp lệ.

> Xem ảnh QR: copy toàn bộ chuỗi `qrCodeBase64` (bắt đầu bằng
> `data:image/png;base64,`), dán vào thanh địa chỉ trình duyệt để xem ảnh.

---

### 5. Pay Booking - Idempotent Replay (same key, no double charge)

Gọi lại **y hệt** request #4, dùng **đúng** `{{paymentIdempotencyKey}}`
vừa sinh (không có pre-request script sinh key mới ở request này).

**Xử lý ở server:** tìm thấy `idempotencyKey` đã tồn tại → trả về
**nguyên payment cũ** (cùng `id`), **không** tạo bản ghi mới, **không**
"trừ tiền"/tạo `ticketCode` mới. Đây là cơ chế chống double-charge khi
client gọi lại API do mạng lag hoặc người dùng bấm nút 2 lần.

**Response `201`** — giống hệt response của request #4 (cùng `id`,
`ticketCode`, `qrCodeBase64`).

---

### 6. Pay Booking - Already Paid, New Idempotency Key (409)

| | |
|---|---|
| Method | `POST` |
| URL | `{{baseUrl}}/api/payments` |

**Request body** — dùng key **mới** (không phải `{{paymentIdempotencyKey}}`
cũ) qua dynamic variable của Postman:
```json
{
    "bookingId": {{paymentBookingId}},
    "idempotencyKey": "idem-{{$guid}}"
}
```

Vì `idempotencyKey` này chưa từng tồn tại, server đi thẳng vào bước
validate booking → thấy `status != PENDING` (đã `PAID` từ request #4) →
`409`.

**Response `409`:**
```json
{
    "status": 409,
    "message": "Chi thanh toan duoc booking dang o trang thai PENDING"
}
```

> So sánh với request #5: cùng gọi lại `/api/payments` trên 1 booking đã
> `PAID`, nhưng #5 dùng **key cũ** → coi là "gọi lại do lag" → trả về kết
> quả cũ (không lỗi); #6 dùng **key mới** → coi là "1 lần thanh toán mới"
> trên booking đã thanh toán rồi → từ chối (409). Đây chính là điểm khác
> biệt cốt lõi mà `idempotencyKey` tạo ra.

---

### 7. Get Ticket By Booking Id

| | |
|---|---|
| Method | `GET` |
| URL | `{{baseUrl}}/api/bookings/{{paymentBookingId}}/ticket` |

Gọi **sau khi** đã thanh toán thành công.

**Response `200`:**
```json
{
    "bookingId": 3,
    "ticketCode": "CB-235VMMZ6",
    "qrCodeBase64": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
    "movieTitle": "Avengers: Doomsday",
    "roomName": "Phong 1",
    "startTime": "2026-08-01T10:00:00+07:00",
    "seats": [
        { "seatId": 103, "rowLabel": "A", "colNumber": 3, "price": 90000.00 },
        { "seatId": 104, "rowLabel": "A", "colNumber": 4, "price": 90000.00 }
    ]
}
```

QR được **sinh lại từ `ticketCode`** mỗi lần gọi endpoint này (không lưu
ảnh trong DB) — `ticketCode` mới là dữ liệu gốc, QR chỉ là biểu diễn trực
quan của nó.

---

## Tổng hợp mã lỗi (HTTP status)

| Status | Khi nào xảy ra | Ở request nào |
|---|---|---|
| `200` | Thành công (GET/PATCH) | Get Booking, History, Cancel, Get Ticket |
| `201` | Tạo mới thành công (POST) | Create Booking, Pay Booking (kể cả replay) |
| `400` | Validate DTO lỗi (thiếu field, sai kiểu) hoặc JSON malformed | Bất kỳ POST nào nếu body sai |
| `404` | `userId`/`showtimeId`/`seatId`/`bookingId` không tồn tại | Create Booking, Get Booking, Pay Booking |
| `409` | Vi phạm quy tắc trạng thái: ghế đã đặt, booking không `PENDING` khi huỷ/trả tiền, booking chưa `PAID` khi xem vé, idempotencyKey dùng cho booking khác | Create Booking (#3), Cancel (#7), Pay Booking (#6), Get Ticket (#3) |

## Sơ đồ trạng thái booking

```
PENDING ──cancel (PATCH .../cancel)──> CANCELLED
   │
   └──pay (POST /api/payments)──> PAID ──> (GET .../ticket khả dụng)
```

`EXPIRED` đã có trong enum `BookingStatus` nhưng chưa có logic tự động
chuyển (cần scheduled job dọn booking `PENDING` quá hạn — để dành Giai
đoạn 2, xem checklist trong [`ROADMAP.md`](./ROADMAP.md)).
