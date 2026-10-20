# Kế Hoạch Phát Triển Dự Án EventHub (Development Plan)

> **Căn cứ**: Tài liệu đặc tả hệ thống tại [docs/project-spec.md](file:///c:/Users/ADMIN/EventHub/docs/project-spec.md).  
> **Mục tiêu**: Xây dựng nền tảng tìm kiếm, đặt vé và quản lý sự kiện **EventHub** chuẩn kiến trúc Production-ready bằng **Java 21**, **Spring Boot 3**, **MySQL**, **Redis**, **Spring Security** và **Docker**.

---

## 📌 Tổng Quan Tiến Độ Dự Án

- **Trạng thái hiện tại**: Đã hoàn thành **Phase 6: Module Payment & Xử Lý Idempotency**. Đang chuẩn bị **Phase 7: Phát Hành Vé Điện Tử (Ticket QR Code) & Check-in**.
- **Base nền tảng**: Đã kiểm thử tự động đạt 100% (49/49 tests pass).
- **GitHub Repository**: [QuanhocIT/EventHub](https://github.com/QuanhocIT/EventHub)

---

## 🗺️ Lộ Trình Triển Khai 10 Giai Đoạn (Theo Spec Mục 18)

### Phase 0: Java & Spring Boot Foundation *(Đã hoàn thành)*

- [x] Thiết lập dự án Spring Boot 3.3.4, Java 21 LTS, Maven Wrapper.
- [x] Cấu hình Docker Compose cho MySQL.
- [x] Xây dựng cấu trúc package chuẩn Layered Architecture.
- [x] Xây dựng `BaseEntity`, `ApiResponse<T>`, `PageResponse<T>`, `GlobalExceptionHandler`.

### Phase 1: Database & Event Management *(Đã hoàn thành)*

- [x] Thiết kế Entity `Event`, ánh xạ JPA với bảng `events`.
- [x] Tạo liên kết `@ManyToOne` giữa `Event` và `User` (`organizer_id`).
- [x] CRUD Event API: Tìm kiếm, lọc theo status, phân trang, lấy chi tiết, tạo, cập nhật, xóa.
- [x] Tự động lấy danh tính `Organizer` từ `Principal` token đăng nhập.
- [x] Domain Event `EventCreatedEvent` + `EventNotificationListener`.

### Phase 2: Authentication & Authorization (RBAC + Ownership) *(Đã hoàn thành)*

- [x] Chuẩn hóa enum `UserRole`: `ROLE_CUSTOMER`, `ROLE_ORGANIZER`, `ROLE_STAFF`, `ROLE_ADMIN`.
- [x] Cấu hình hạt nhân xác thực Spring Security 6 + JWT Stateless.
- [x] Seed tài khoản mẫu cho 4 vai trò để sẵn sàng kiểm thử (`customer`, `organizer`, `staff`, `admin`).
- [x] Phân quyền theo vai trò (`@PreAuthorize`):
  - Chỉ `ROLE_ORGANIZER` và `ROLE_ADMIN` được tạo sự kiện.
  - `ROLE_CUSTOMER` chỉ được xem và đặt vé.
  - `ROLE_STAFF` được quyền soát vé (check-in).
- [x] Kiểm tra quyền sở hữu (Ownership Check): Chỉ người tạo sự kiện hoặc Admin mới có quyền cập nhật (`PUT`) hoặc xóa (`DELETE`) sự kiện.
- [x] Viết Unit & Integration Test kiểm thử các kịch bản 403 Forbidden.

### Phase 3: Module Venue & Seat Map (Địa Điểm & Ghế) *(Đã hoàn thành)*

- [x] Thiết kế Entity `Venue` (id, name, address, capacity).
- [x] Thiết kế Entity `Seat` (id, venue_id, row, number, zone: VIP, Standard, Balcony,...).
- [x] Thiết kế Entity `EventSeat` (id, event_id, seat_id, status: `AVAILABLE`, `HELD`, `SOLD`, `BLOCKED`, price).
- [x] CRUD Venue và khởi tạo sơ đồ ghế tự động cho địa điểm.
- [x] Gán địa điểm cho sự kiện và sinh bản sao `EventSeat`.
- [x] API lấy sơ đồ ghế theo từng sự kiện (`GET /api/v1/events/{id}/seats`).

### Phase 4: Module TicketType & Booking Cơ Bản *(Đã hoàn thành)*

- [x] Thiết kế Entity `TicketType` (Early Bird, Standard, VIP,... có giá, số lượng, min/max per order).
- [x] Thiết kế Entity `Booking` và `BookingItem` với mã đơn dạng `EVH-YYYYMMDD-XXXX`.
- [x] API quản lý loại vé (`POST`, `GET /api/v1/events/{eventId}/ticket-types`).
- [x] API tạo Booking (`POST /api/v1/bookings`) với validation nghiệp vụ, trừ quota vé và transaction toàn vẹn.
- [x] API xem lịch sử đặt vé (`GET /api/v1/bookings/my`) và xem chi tiết theo mã đơn.
- [x] API hủy đơn hàng (`POST /api/v1/bookings/{code}/cancel`) giải phóng tồn kho vé và hoàn trạng thái ghế.

### Phase 5: Xử Lý Concurrency & Tạm Giữ Ghế (Temporary Seat Hold) *(Đã hoàn thành)*

- [x] Tích hợp Redis vào dự án (`spring-boot-starter-data-redis`) và bổ sung Redis vào `docker-compose.yml`.
- [x] Cơ chế Temporary Seat Hold với Redis TTL (5 phút): `seat_hold:{eventId}:{seatId}`.
- [x] Xử lý Race Condition / Overselling bằng **Pessimistic Locking** (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) tại tầng Database.
- [x] Tác vụ định kỳ `@Scheduled` tự động giải phóng ghế và hoàn trả tồn kho vé khi đơn hàng quá hạn thanh toán (`BookingCleanupJob`).

### Phase 6: Module Payment & Xử Lý Idempotency *(Đã hoàn thành)*

- [x] Thiết kế Entity `Payment` (booking_id, provider, transaction_code, amount, status: `PENDING`, `SUCCESS`, `FAILED`, idempotency_key).
- [x] Flow thanh toán sandbox giả lập (`POST /api/v1/payments/create`, `POST /api/v1/payments/process-sandbox`).
- [x] Xử lý callback/webhook idempotent với `Idempotency-Key` (ngăn chặn duplicate payment khi client retry hoặc network lag).
- [x] Tự động xác nhận `Booking` sang `CONFIRMED`, chuyển ghế sang `SOLD` và dọn dẹp Redis hold khi thanh toán thành công.

### Phase 7: Phát Hành Vé Điện Tử (Ticket QR Code) & Check-in

- [ ] Thiết kế Entity `Ticket` liên kết với `BookingItem`.
- [ ] Thư viện sinh mã QR (ZXing) chứa token vé an toàn.
- [ ] API soát vé Check-in cho `ROLE_STAFF` tại cổng sự kiện: Chuyển trạng thái vé sang `USED`, từ chối check-in lần 2.

### Phase 8: Notification & Xử Lý Bất Đồng Bộ (@Async)

- [ ] Thông báo qua Email khi đặt vé thành công kèm mã QR Code.
- [ ] Thông báo khi sự kiện thay đổi lịch hoặc bị hủy.

### Phase 9: Dashboard Thống Kê & Báo Cáo Doanh Thu

- [ ] Organizer Dashboard: Doanh thu theo thời gian, số vé đã bán, tỷ lệ lấp đầy ghế.
- [ ] Admin Dashboard: Toàn cảnh người dùng, số lượng sự kiện, tổng giao dịch.

### Phase 10: Docker & Triển Khai Hoàn Thiện

- [ ] Docker Compose chạy đa dịch vụ: Spring Boot App + MySQL 8.4 + Redis 7.
- [ ] Cấu hình biến môi trường production, tối ưu connection pool HikariCP.

---

## 📚 Kỹ Năng Phỏng Vấn Trọng Tâm Từng Module

- **Security**: Phân biệt Role vs Authority; Cách thiết kế Custom Ownership Check trong Spring Security; Ngăn chặn lỗ hổng IDOR.
- **Concurrency**: Bản chất của Race Condition khi 2 người cùng click đặt 1 ghế; Pessimistic Locking vs Optimistic Locking; Distributed Lock với Redis Redisson.
- **Idempotency**: Tại sao API thanh toán phải idempotent? Cơ chế Idempotency Key.
