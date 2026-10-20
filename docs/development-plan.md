# Kế Hoạch Phát Triển Dự Án EventHub (Development Plan)

> **Căn cứ**: Tài liệu đặc tả hệ thống tại [docs/project-spec.md](file:///c:/Users/ADMIN/EventHub/docs/project-spec.md).  
> **Mục tiêu**: Xây dựng nền tảng tìm kiếm, đặt vé và quản lý sự kiện **EventHub** chuẩn kiến trúc Production-ready bằng **Java 21**, **Spring Boot 3**, **MySQL**, **Redis**, **Spring Security** và **Docker**.

---

## 📌 Tổng Quan Tiến Độ Dự Án

- **Trạng thái hiện tại**: Đã hoàn thành **Phase 8: Notification & Xử Lý Bất Đồng Bộ (@Async)**. Đang tiến hành **Phase 9: Dashboard Thống Kê & Báo Cáo Doanh Thu**.
- **Base nền tảng**: Đã kiểm thử tự động đạt 100% (59/59 tests pass).
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

### Phase 7: Phát Hành Vé Điện Tử (Ticket QR Code) & Check-in *(Đã hoàn thành)*

- [x] Thiết kế Entity `Ticket` liên kết với `BookingItem` và `EventSeat`.
- [x] Tích hợp thư viện Google ZXing sinh mã QR Code Base64 (`QrCodeService`).
- [x] Tự động phát hành vé khi thanh toán đơn hàng thành công (`TicketService.generateTicketsForBooking`).
- [x] API xem vé của người dùng (`GET /api/v1/tickets/my`) kèm hình ảnh mã QR.
- [x] API soát vé Check-in tại cổng (`POST /api/v1/tickets/{ticketCode}/check-in`) cho `ROLE_STAFF`: Chuyển trạng thái vé sang `USED`, ghi nhận thời gian & nhân viên, từ chối check-in lần 2 (Anti-passback).

### Phase 8: Notification & Xử Lý Bất Đồng Bộ (@Async) *(Đã hoàn thành)*

- [x] Cấu hình Spring Boot `@EnableAsync` với ThreadPoolTaskExecutor chuyên dụng (`AsyncConfig`).
- [x] Dịch vụ `EmailService` gửi email xác nhận đặt vé và thông báo hủy sự kiện bất đồng bộ.
- [x] Kiến trúc Domain Event: Bắn `BookingConfirmedEvent` và lắng nghe qua `BookingNotificationListener`.

### Phase 9: Dashboard Thống Kê & Báo Cáo Doanh Thu *(Đã hoàn thành)*

- [x] Organizer Dashboard: Doanh thu thực tế, số vé phát hành, số vé check-in, tỷ lệ check-in theo sự kiện (`GET /api/v1/dashboard/organizer`, `GET /api/v1/dashboard/organizer/events/{eventId}`).
- [x] Admin Dashboard: Toàn cảnh người dùng theo vai trò (Customer, Organizer, Staff), số lượng sự kiện, tổng giao dịch & doanh thu toàn sàn (`GET /api/v1/dashboard/admin`).
- [x] Tối ưu JPQL Aggregation queries với `COALESCE(SUM(...), 0)` tránh trả về NULL và kiểm soát quyền truy cập chi tiết.

### Phase 10: Docker & Triển Khai Hoàn Thiện *(Đã hoàn thành)*

- [x] Docker Compose chạy đa dịch vụ chuẩn Microservice: Spring Boot App + MySQL 8.4 LTS + Redis 7 Alpine (`docker-compose.yml`).
- [x] Multi-stage build `Dockerfile` tối ưu layer caching và sử dụng Eclipse Temurin Java 21 Alpine JRE với non-root user.
- [x] Cấu hình cấu hình `application-prod.yml` tối ưu HikariCP Connection Pool (20 max pool size, timeout 20s).
- [x] Cấu hình JVM Flags tối ưu containerized (`-XX:+UseG1GC`, `-XX:MaxRAMPercentage=75.0`).
- [x] Đóng gói và kiểm tra toàn diện runnable JAR (`mvnw clean package`).

### Phase 11: Module Category & Bộ Lọc Sự Kiện Nâng Cao (Dynamic Specification) *(Đã hoàn thành)*

- [x] Thiết kế Entity `Category` (id, name, slug, description, iconUrl, active) và CRUD API.
- [x] Liên kết `@ManyToOne` giữa `Event` và `Category`.
- [x] Xây dựng bộ lọc tìm kiếm đa tiêu chí bằng Spring Data JPA Specification (`EventSpecification`): Lọc theo keyword, danh mục, khoảng giá (`minPrice`, `maxPrice`), khoảng thời gian diễn ra (`startDate`, `endDate`), địa điểm.
- [x] Hỗ trợ phân trang và sắp xếp động theo tiêu chuẩn Production.

### Phase 12: Module Coupon / Voucher Khuyến Mãi & Hệ Thống Data Seeder *(Đã hoàn thành)*

- [x] Thiết kế Entity `Coupon` hỗ trợ 2 hình thức: Giảm theo phần trăm (`PERCENTAGE` kèm trần giảm giá tối đa) và Giảm tiền trực tiếp (`FIXED_AMOUNT`).
- [x] Cơ chế kiểm soát quota sử dụng, hạn dùng, giá trị đơn tối thiểu và giới hạn sự kiện áp dụng.
- [x] Sử dụng **Pessimistic Write Lock (`@Lock(LockModeType.PESSIMISTIC_WRITE)`)** khi áp dụng mã giảm giá để loại bỏ hoàn toàn Race Condition khi nhiều khách hàng cùng áp dụng mã ưu đãi số lượng có hạn.
- [x] Cơ chế hoàn trả lượt dùng (`releaseCouponUsage`) khi đơn hàng bị hủy (`cancelBooking`).
- [x] Tích hợp tự động vào quy trình đặt vé `Booking` (tính toán `subtotalAmount`, `discountAmount`, `totalAmount`).
- [x] Xây dựng `DataSeeder` tự động nạp sẵn dữ liệu mẫu thực tế: 5 địa điểm nổi tiếng Việt Nam (Mỹ Đình, NCC, Phú Thọ, Nhà hát Lớn, SECC), 6 danh mục sự kiện, 6 sự kiện phong phú đầy đủ các hạng vé, 3 mã coupon và đơn hàng demo với e-ticket QR Code để Dashboard hiển thị số liệu trực quan ngay khi khởi động.

### Phase 13: Module Quản Lý Tài Khoản Cá Nhân & Quản Trị Người Dùng (Admin User Management) *(Đã hoàn thành)*

- [x] Bổ sung các trường `phoneNumber`, `avatarUrl` vào Entity `User`.
- [x] Xây dựng API quản lý hồ sơ cá nhân: `GET /api/v1/users/me` và `PUT /api/v1/users/me` (Cập nhật họ tên, SĐT, avatar).
- [x] Quy trình đổi mật khẩu an toàn: `PUT /api/v1/users/me/change-password` (Kiểm tra mật khẩu cũ bằng BCrypt, so khớp xác nhận mật khẩu, không cho phép đặt lại mật khẩu cũ).
- [x] Bộ công cụ quản trị người dùng cho `ROLE_ADMIN`:
  - Tìm kiếm người dùng đa tiêu chí (keyword, role, enabled), phân trang và sắp xếp: `GET /api/v1/users`.
  - Khóa / Kích hoạt tài khoản: `PATCH /api/v1/users/{id}/status` (Bảo vệ nghiệp vụ: Ngăn chặn Admin tự khóa tài khoản của chính mình).
  - Thay đổi phân quyền vai trò: `PATCH /api/v1/users/{id}/role` (Bảo vệ nghiệp vụ: Ngăn chặn Admin tự hạ quyền của chính mình).
- [x] Viết Unit Tests toàn diện với 12 test cases cho `UserServiceTest`.

---

## 📚 Kỹ Năng Phỏng Vấn Trọng Tâm Từng Module

- **Security (Phase 1-2)**: Phân biệt Role vs Authority; Cách thiết kế Custom Ownership Check trong Spring Security; Ngăn chặn triệt để lỗ hổng IDOR; Stateless Authentication với JWT Access & Refresh Token.
- **Data Modeling & Concurrency (Phase 3-5)**: Bản chất của Race Condition khi 2 người cùng click đặt 1 ghế; Pessimistic Locking (`@Lock(PESSIMISTIC_WRITE)`) vs Optimistic Locking (`@Version`); Distributed Temporary Hold với Redis TTL và cơ chế auto-release scheduler.
- **Idempotency & Transactions (Phase 6)**: Tại sao API thanh toán và webhook bắt buộc phải Idempotent? Sử dụng Idempotency Key và Database Unique Constraint để chống double-spending khi mạng chập chờn.
- **QR Code & Gate Check-in (Phase 7)**: Sinh mã QR Code Base64 nhúng bảo mật; Cơ chế Anti-passback kiểm soát vé tại cổng tránh gian lận.
- **Asynchronous & Decoupling (Phase 8)**: Quản lý ThreadPoolTaskExecutor; Xử lý Domain Events với `@Async` và `@EventListener` tách biệt nghiệp vụ chính khỏi thông báo (email/SMS).
- **Aggregations & Analytics (Phase 9)**: Tối ưu hóa truy vấn tổng hợp báo cáo bằng JPQL/Native SQL với `COALESCE` và index composite, tránh N+1 Query.
- **Containerization & Production (Phase 10)**: Multi-stage Docker build giảm thiểu image size và tăng tính bảo mật (Non-root user); Cấu hình JVM GC & RAM percentage tối ưu trong môi trường container Kubernetes/Docker.
- **Dynamic Queries & Criteria API (Phase 11)**: Tại sao nên dùng Spring Data JPA `Specification` thay cho việc nối chuỗi String query? Cơ chế compile-time type safety và chống SQL Injection.
- **Promotions & Concurrency Safety (Phase 12)**: Giải quyết bài toán concurrency với mã giảm giá số lượng có hạn; Quản lý vòng đời khuyến mãi và cơ chế bù trừ (compensation transaction) khi đơn hàng hủy.
- **User Management & Business Safety Guards (Phase 13)**: Thiết kế API đổi mật khẩu an toàn; Kỹ thuật Self-Protection Guards (ngăn chặn Admin tự khóa hoặc tự tước quyền quản trị của chính mình, bảo vệ tính toàn vẹn hệ thống).
