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

### Phase 14: Module Đánh Giá & Xếp Hạng Sự Kiện (Event Reviews & Ratings) *(Đã hoàn thành)*

- [x] Thiết kế Entity `Review` liên kết `Event` và `User` với ràng buộc duy nhất `@UniqueConstraint(name = "uk_reviews_user_event", columnNames = {"user_id", "event_id"})` đảm bảo mỗi người dùng chỉ đánh giá 1 lần cho mỗi sự kiện (có hỗ trợ chỉnh sửa).
- [x] **Verified Attendee Guard (Bộ lọc kiểm chứng người tham gia thực tế)**:
  - Chỉ cho phép đánh giá khi sự kiện đã bắt đầu diễn ra (`startTime <= now`).
  - Kiểm tra điều kiện bắt buộc: Người dùng phải có đơn hàng vé xác nhận (`BookingStatus.CONFIRMED`) hoặc có vé đã check-in qua cổng (`TicketStatus.USED`). Ngăn chặn triệt để review ảo hoặc spam phá hoại uy tín sự kiện.
- [x] **Dynamic Rating Aggregation & Caching**:
  - Truy vấn JPQL tính điểm trung bình (`AVG(rating)`) và tổng số đánh giá (`COUNT(rating)`), làm tròn 2 chữ số thập phân (`4.85/5.0`).
  - Tự động đồng bộ và lưu cache trực tiếp vào trường `averageRating` và `reviewCount` trên Entity `Event` mỗi khi có review mới, sửa hoặc xóa. Tối ưu hiệu năng đọc trang chủ và danh sách sự kiện mà không phải tính lại toàn bảng reviews.
- [x] Hệ thống API toàn diện:
  - `GET /api/v1/events/{eventId}/reviews`: Xem danh sách đánh giá của sự kiện (Công khai, phân trang, xếp theo mới nhất).
  - `GET /api/v1/events/{eventId}/reviews/summary`: Báo cáo xếp hạng tổng quan và phân bổ chi tiết số sao (từ 1 sao đến 5 sao).
  - `GET /api/v1/events/{eventId}/reviews/my-review`: Lấy đánh giá của chính người dùng hiện tại đối với sự kiện.
  - `POST /api/v1/events/{eventId}/reviews`: Gửi đánh giá mới (1-5 sao, bình luận tối đa 2000 ký tự).
  - `PUT /api/v1/reviews/{reviewId}`: Chỉnh sửa đánh giá (chỉ tác giả mới có quyền sửa).
  - `DELETE /api/v1/reviews/{reviewId}`: Xóa đánh giá (cho phép tác giả hoặc `ROLE_ADMIN`).
- [x] Tích hợp nạp sẵn dữ liệu mẫu trong `DataSeeder` với verified review demo kèm số sao thực tế.
- [x] Viết bộ Unit Tests hoàn chỉnh `ReviewServiceTest` với 15 test cases, nâng tổng số kiểm thử toàn dự án lên **109 tests (100% PASS)**.

### Phase 15: Module Hồ Sơ Đơn Vị Tổ Chức (Organizer Profile, Brand Page & Follow System) *(Đã hoàn thành)*

- [x] Thiết kế Entity `OrganizerProfile` quản lý thương hiệu ban tổ chức: Tên tổ chức, tiểu sử giới thiệu (bio), logoUrl, bannerUrl, website, email & số điện thoại liên hệ, liên kết mạng xã hội (Facebook, LinkedIn), huy hiệu xác thực (`verified`).
- [x] Thiết kế Entity `OrganizerFollow` lưu vết quan hệ người dùng theo dõi ban tổ chức với ràng buộc duy nhất `@UniqueConstraint(name = "uk_user_organizer_follow", columnNames = {"user_id", "organizer_id"})`.
- [x] **Hệ thống API toàn diện**:
  - `GET /api/v1/organizers/{id}`: Trang công khai của ban tổ chức (kèm thống kê: tổng số sự kiện, số sự kiện đang mở, số người theo dõi, điểm đánh giá trung bình từ người tham dự, trạng thái đã theo dõi hay chưa).
  - `GET /api/v1/organizers/{id}/events`: Danh sách các sự kiện đang mở bán của ban tổ chức (phân trang, sắp xếp theo thời gian mới nhất).
  - `GET /api/v1/organizers/me`: Lấy thông tin hồ sơ của ban tổ chức đang đăng nhập.
  - `PUT /api/v1/organizers/me`: Cập nhật thông tin thương hiệu (chỉ dành cho `ROLE_ORGANIZER` hoặc `ROLE_ADMIN`).
  - `POST /api/v1/organizers/{id}/follow`: Theo dõi ban tổ chức (tự động chặn tự theo dõi chính mình, idempotent nếu đã theo dõi).
  - `DELETE /api/v1/organizers/{id}/follow`: Bỏ theo dõi ban tổ chức.
  - `GET /api/v1/organizers/following`: Danh sách các ban tổ chức người dùng đang theo dõi (phân trang).
  - `PATCH /api/v1/organizers/{id}/verify`: Quản trị viên (`ROLE_ADMIN`) cấp hoặc thu hồi huy hiệu xác thực (Verified Badge).
- [x] Tích hợp nạp sẵn dữ liệu mẫu trong `DataSeeder` với hồ sơ thương hiệu "Nexus Tech Media & Events" và quan hệ theo dõi mẫu.
- [x] Viết bộ Unit Tests toàn diện `OrganizerServiceTest` với 12 test cases, nâng tổng số kiểm thử toàn dự án lên **121 tests (100% PASS)**.

### Phase 16: Module Audit Logging & Activity Trail (Spring AOP & Security Auditing) *(Đã hoàn thành)*

- [x] Thiết kế Entity `AuditLog`: Lưu vết hành động (`action`), đối tượng chịu tác động (`entityType`, `entityId`), người thực hiện (`username`, `userId`), địa chỉ mạng IP khách (`ipAddress` trích xuất `X-Forwarded-For`), trình duyệt/thiết bị (`userAgent`), trạng thái (`SUCCESS`/`FAILED`), chi tiết lỗi và thời gian thực thi mili-giây (`executionTimeMs`).
- [x] Xây dựng Custom Annotation `@Auditable` và Aspect `AuditLogAspect` với `@Around` advice bắt tự động mọi thao tác nhạy cảm:
  - Khóa/mở tài khoản người dùng (`USER_STATUS_UPDATE`).
  - Thay đổi phân quyền vai trò (`USER_ROLE_UPDATE`).
  - Cấp huy hiệu xác thực tổ chức (`ORGANIZER_VERIFY`).
- [x] Độc lập giao dịch bằng `@Transactional(propagation = Propagation.REQUIRES_NEW)` đảm bảo nhật ký kiểm toán không bị rollback ngoài ý muốn nếu nghiệp vụ xảy ra lỗi.
- [x] API Quản trị viên tra cứu lịch sử hành vi `GET /api/v1/admin/audit-logs` hỗ trợ lọc động Specification đa tiêu chí (theo action, username, entityType, phân trang và sắp xếp).
- [x] Tích hợp nạp sẵn các bản ghi audit log mẫu trong `DataSeeder`.
- [x] Viết bộ Unit Tests cho `AuditLogServiceTest`.

### Phase 17: Module Xuất Báo Cáo Doanh Thu & Danh Sách Khách Tham Gia (Apache POI Excel Export) *(Đã hoàn thành)*

- [x] Tích hợp thư viện Apache POI (`poi`, `poi-ooxml 5.3.0`) tối ưu bộ nhớ khi render bảng tính `.xlsx`.
- [x] **Xuất danh sách khách tham dự (`exportEventAttendees`)**:
  - `GET /api/v1/export/events/{eventId}/attendees` (Quyền: Ban tổ chức sự kiện hoặc Admin).
  - Định dạng bảng tính chuyên nghiệp: Header thương hiệu xanh dương đậm (`ROYAL_BLUE`), font chữ đậm, căn giữa.
  - Cột dữ liệu đầy đủ: STT, Mã vé, Mã đơn hàng, Họ tên, Email, Số điện thoại, Hạng vé, Vị trí ghế, Trạng thái vé (VALID/USED), Thời gian check-in tại cổng, Ngày mua vé.
  - Tự động căn chỉnh độ rộng cột (`sheet.autoSizeColumn`).
- [x] **Xuất báo cáo tài chính & doanh thu theo năm (`exportOrganizerRevenueReport`)**:
  - `GET /api/v1/export/organizers/{organizerId}/revenue?year=2026` (Quyền: Đơn vị tổ chức sở hữu hoặc Admin).
  - Bảng tổng kết số liệu: Tổng số đơn thành công, Tổng tiền vé, Tổng giảm giá voucher, Thực thu ròng.
  - Bảng chi tiết 12 tháng: Số lượng đơn hàng, Doanh thu gộp, Chiết khấu, Doanh thu ròng theo từng tháng.
- [x] Viết bộ Unit Tests cho `ExcelExportServiceTest` xác thực cấu trúc byte workbook `.xlsx`.
- [x] Nâng tổng số kiểm thử tự động toàn dự án lên **128 tests (100% PASS)**.

### Phase 18: Frontend Admin & Organizer Management Portal (Dashboard, Biểu Đồ Doanh Thu, Quản Trị User, Audit Trail & Xuất Báo Cáo Excel) *(Đã hoàn thành)*

- [x] **Xây dựng Trung Tâm Điều Hành & Quản Trị Toàn Diện (`/dashboard`, `/admin`)**:
  - Giao diện trực quan tích hợp switcher đa góc nhìn: **Góc nhìn Quản Trị Viên (Admin)** và **Góc nhìn Ban Tổ Chức (Organizer)**.
  - Tích hợp điều hướng trực tiếp trên [Navbar.vue](file:///c:/Users/ADMIN/EventHub/frontend/src/components/Navbar.vue) (desktop menu, user dropdown và mobile menu).
- [x] **Tab 1: Tổng Quan KPI & Biểu Đồ Doanh Thu (Overview & Analytics)**:
  - 4 Thẻ KPI động: Doanh thu thực tế, Tổng số vé phát hành & tỷ lệ check-in, Sự kiện mở bán, Số lượng người dùng & đơn hàng.
  - Biểu đồ cột tương tác trực quan 12 tháng (`monthlyStats`) hiển thị doanh thu kèm tooltip tiền tệ VNĐ và chỉ số tăng trưởng.
  - Phân bổ người dùng theo vai trò (Customer, Organizer, Staff) và huy hiệu bảo mật Anti-Passback.
- [x] **Tab 2: Sự Kiện & Phân Tích Vé Chi Tiết (Events & Attendance Analytics)**:
  - Bảng danh sách sự kiện đầy đủ: Tên, banner, ngày giờ, địa điểm, trạng thái mở bán, giá vé.
  - Modal phân tích chi tiết từng sự kiện: Doanh thu, số vé phát hành, lượt khách đã check-in qua cổng và tỷ lệ tham dự.
  - Tích hợp nút **Xuất danh sách khách tham dự (.xlsx)** gọi API backend `/api/v1/export/events/{id}/attendees`.
- [x] **Tab 3: Quản Lý Người Dùng & Phân Quyền (Admin User Administration)**:
  - Bộ lọc đa năng: Tìm kiếm theo họ tên, username, email, SĐT; Lọc theo vai trò (Customer/Organizer/Staff/Admin) và trạng thái (Hoạt động/Đã khóa).
  - Khóa / Mở khóa tài khoản an toàn với cơ chế bảo vệ Self-Protection Guard (Admin không thể tự khóa chính mình).
  - Phân quyền nhanh vai trò người dùng (Customer <-> Staff <-> Organizer <-> Admin).
- [x] **Tab 4: Nhật Ký Kiểm Toán An Ninh (Security Audit Trail)**:
  - Bảng tra cứu toàn bộ hành vi nhạy cảm: Thời gian, Action (`USER_STATUS_UPDATE`, `USER_ROLE_UPDATE`, `ORGANIZER_VERIFY`, `EXPORT_ATTENDEES`, `USER_LOGIN_FAILED`), Người thực hiện, Đối tượng, IP khách, Trạng thái (SUCCESS/FAILED) và Thời gian thực thi mili-giây (`ms`).
- [x] **Xuất Báo Cáo Tài Chính Năm (.xlsx)**:
  - Nút xuất file Excel báo cáo doanh thu tài chính tích hợp gọi API backend `/api/v1/export/organizers/{id}/revenue`.

### Phase 19: Triển Khai Chuyên Sâu Toàn Diện 4 Vai Trò (Admin, Organizer, Staff, Customer) *(Đã hoàn thành)*

- [x] **Vai Trò 1: Khách Hàng (Customer Portal - `/profile`)**:
  - Giao diện 4 phân hệ hiện đại: Thông tin tài khoản, Đổi mật khẩu an toàn, Quản lý đơn hàng & vé điện tử, Danh sách ban tổ chức đang theo dõi.
  - Tích hợp API hủy đơn hàng với modal xác nhận và cập nhật trạng thái đơn real-time.
  - Tích hợp API hủy theo dõi (`unfollow`) trực tiếp từ danh sách ban tổ chức.
- [x] **Vai Trò 2: Nhân Viên Soát Vé (Staff Gate Terminal - `/dashboard?tab=staff_scanner`)**:
  - Máy soát vé điện tử tại cổng: Hỗ trợ quét mã QR bằng camera và nhập mã vé thủ công.
  - Tích hợp chuẩn Web Audio API tổng hợp âm thanh phản hồi âm học (Beep thanh 880Hz khi hợp lệ, Buzzer trầm 220Hz khi lỗi).
  - Kết nối REST API `POST /api/v1/tickets/{ticketCode}/check-in` với cơ chế Anti-Passback ngăn chặn tuyệt đối vé quay vòng.
  - Bảng nhật ký cổng thời gian thực (Gate Activity Log) ghi lại từng lượt soát vé thành công/thất bại.
- [x] **Vai Trò 3: Ban Tổ Chức (Organizer Management Suite)**:
  - Form Wizard tạo sự kiện kết nối trực tiếp backend `POST /api/v1/events` với chọn địa điểm, ngày giờ, số lượng vé và upload banner.
  - Quản lý mã giảm giá sự kiện (`org_vouchers`) kết nối `POST /api/v1/coupons` với chiết khấu theo % hoặc số tiền cố định.
  - Quản trị hồ sơ thương hiệu (`org_profile`) cho phép cập nhật tên tổ chức, tiểu sử, website, hotline kết nối `GET/PUT /api/v1/organizers/me`.
  - Tích hợp xuất danh sách khách tham dự và báo cáo doanh thu năm ra file Excel `.xlsx`.
- [x] **Vai Trò 4: Quản Trị Viên Hệ Thống (System Admin)**:
  - Quản lý danh mục sự kiện toàn sàn (`admin_categories`): Thêm danh mục mới, xem số lượng sự kiện thuộc danh mục, xóa danh mục qua REST API.
  - Quản lý toàn sàn mã ưu đãi (`admin_coupons`).
  - Quản trị người dùng với cơ chế Self-Protection Guard chống tự khóa/tự giáng cấp bản thân.
  - Duyệt huy hiệu tích xanh cho đơn vị tổ chức uy tín (`PATCH /api/v1/organizers/{id}/verify`).
  - Giám sát an ninh hệ thống với Security Audit Trail.
- [x] **Điều Hướng & Trải Nghiệm Người Dùng (Navigation & UX)**:
  - Cập nhật [Navbar.vue](file:///c:/Users/ADMIN/EventHub/frontend/src/components/Navbar.vue) hiển thị shortcut thông minh theo từng vai trò (Trang cá nhân, Cổng soát vé Staff, Quản lý sự kiện Organizer, Quản trị Admin).
  - Cập nhật Router guards cho phép `ROLE_STAFF` truy cập trực tiếp bảng điều khiển chuyên biệt.

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
- **Social Proof & Verified Attendee Pattern (Phase 14)**: Cơ chế chống đánh giá ảo (Verified Attendee Guard); Kỹ thuật Denormalization / Summary Caching trên bảng `events` để tối ưu chi phí truy vấn đọc cao (read-heavy) so với chi phí ghi thấp (write-rare) trong bài toán kiến trúc hệ thống lớn.
- **Social Graph & Entity Separation (Phase 15)**: Tách bạch bảng `users` (danh tính/xác thực) và `organizer_profiles` (thương hiệu/kinh doanh) theo nguyên lý Single Responsibility Principle (SRP); Thiết kế quan hệ nhiều-nhiều tự tham chiếu (`Many-to-Many` qua bảng liên kết `organizer_follows`) với ràng buộc Composite Unique Index và Idempotent follow operations.
- **Aspect-Oriented Programming & Security Auditing (Phase 16)**: Sử dụng Spring AOP (`@Around`) để tách bạch Cross-cutting Concerns (Logging/Auditing) khỏi Core Business Logic; Kỹ thuật trích xuất Header `X-Forwarded-For` đằng sau Reverse Proxy/Load Balancer; Tách biệt Transaction Boundary với `Propagation.REQUIRES_NEW` để đảm bảo log kiểm toán luôn được ghi lại kể cả khi giao dịch chính bị rollback do ngoại lệ.
- **Binary Streaming & Spreadsheet Generation (Phase 17)**: Kỹ thuật xuất file nhị phân trực tiếp ra HTTP Response Stream với MIME type `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` và header `Content-Disposition`; Quản lý bộ nhớ JVM khi tạo tài liệu Excel lớn (SXSSF vs XSSF) bằng Apache POI.
- **Enterprise Dashboard & Role-based Portals (Phase 18)**: Thiết kế Single-Page Dashboard với cơ chế State Management đa góc nhìn (Admin vs Organizer); Tối ưu hiển thị dữ liệu lớn với client-side filtering; Tích hợp tải file nhị phân trực tiếp từ REST API thông qua Blob Object URL và trigger download an toàn.
