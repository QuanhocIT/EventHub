# Kế Hoạch Phát Triển Dự Án EventHub (Development Plan)

> **Mục tiêu**: Xây dựng hệ thống Backend hoàn chỉnh cho nền tảng quản lý sự kiện và đặt vé **EventHub**, sử dụng **Java 21**, **Spring Boot 3** và **MySQL**, đồng thời rèn luyện tư duy thiết kế kiến trúc, xử lý concurrency và trả lời phỏng vấn chuyên sâu.

---

## 📌 Tổng Quan Tiến Độ Dự Án

- **Trạng thái hiện tại**: Đang ở **Giai đoạn 2 (Security & Authorization)**.
- **Base nền tảng**: Đã kiểm thử tự động đạt 100% (11/11 tests pass).
- **GitHub Repository**: [QuanhocIT/EventHub](https://github.com/QuanhocIT/EventHub)

---

## 🗺️ Lộ Trình Phát Triển Từng Giai Đoạn (Milestones)

### Giai Đoạn 1: Chuẩn Hóa Nền Tảng & Liên Kết Người Tổ Chức (Organizer)

- [x] Đọc cấu hình JWT, CORS từ biến môi trường (`.env`) thay vì hardcode.
- [x] Tạo quan hệ `@ManyToOne` giữa `Event` và `User` (`organizer_id`).
- [x] Tự động lấy danh tính người dùng hiện tại từ Spring Security `Principal` khi tạo sự kiện.
- [x] Bổ sung cấu hình `@ActiveProfiles("test")` và viết bộ unit test với H2 in-memory.
- [x] Kết nối và đẩy source code khởi tạo lên GitHub repository.

---

### Giai Đoạn 2: Phân Quyền Vai Trò (RBAC) & Kiểm Tra Quyền Sở Hữu (Ownership)

- [ ] Bật `@EnableMethodSecurity` và áp dụng phân quyền bằng annotation `@PreAuthorize`.
- [ ] Phân quyền theo vai trò (Role-based):
  - `ROLE_USER`: Xem danh sách sự kiện, xem chi tiết sự kiện, đặt vé.
  - `ROLE_ORGANIZER`: Tạo sự kiện, quản lý sự kiện của chính mình.
  - `ROLE_ADMIN`: Toàn quyền quản trị hệ thống.
- [ ] Kiểm tra quyền sở hữu (Ownership Check):
  - Người dùng chỉ được sửa (`PUT`) hoặc xóa (`DELETE`) sự kiện do chính mình tạo ra.
  - Ngoại lệ: `ADMIN` có quyền can thiệp vào bất kỳ sự kiện nào.
- [ ] Viết Unit & Integration Test kiểm thử các kịch bản 403 Forbidden.

---

### Giai Đoạn 3: Quản Lý Danh Mục (Category) & Lọc Sự Kiện Nâng Cao

- [ ] Thiết kế Entity `Category` (id, name, slug, description, icon).
- [ ] Thiết lập quan hệ `@ManyToOne` từ `Event` tới `Category`.
- [ ] CRUD Category API (chỉ `ADMIN` có quyền thêm/sửa/xóa).
- [ ] Nâng cấp API tìm kiếm sự kiện:
  - Lọc theo khoảng ngày (from date - to date).
  - Lọc theo `categoryId`, khoảng giá vé (`minPrice` - `maxPrice`), địa điểm, trạng thái (`status`).
  - Viết truy vấn động sử dụng **Spring Data JPA Specifications** (hoặc Criteria API).

---

### Giai Đoạn 4: Quản Lý Loại Vé & Số Lượng Vé (Ticket Types & Inventory)

- [ ] Thiết kế Entity `TicketType` (id, event_id, name, price, total_quantity, available_quantity, sales_start_time, sales_end_time).
  - Ví dụ loại vé: Early Bird, Standard, VIP.
- [ ] Một sự kiện có nhiều loại vé (`@OneToMany`).
- [ ] API cấu hình các loại vé cho sự kiện (dành cho Organizer).
- [ ] Validation nghiệp vụ:
  - Tổng số lượng vé của các `TicketType` không vượt quá `capacity` của `Event`.
  - Giá vé không âm.
  - Thời gian mở bán vé phải trước hoặc trùng với ngày diễn ra sự kiện.

---

### Giai Đoạn 5: Đặt Vé (Booking / Order) & Kiểm Soát Đồng Thời (Concurrency Control)

> 🔥 *Đây là module trọng tâm quan trọng nhất để ghi điểm khi phỏng vấn Senior Java Backend!*

- [ ] Thiết kế Entity `Order` và `OrderItem`:
  - `Order`: id, order_code, user_id, total_amount, status (`PENDING`, `CONFIRMED`, `CANCELLED`, `EXPIRED`), created_at.
  - `OrderItem`: id, order_id, ticket_type_id, quantity, unit_price.
  - `Ticket`: Vé điện tử cụ thể sinh ra sau khi thanh toán thành công (ticket_code, qr_code, status: `VALID`, `USED`, `CANCELLED`).
- [ ] Nghiệp vụ Đặt Vé:
  - Kiểm tra số lượng vé còn lại (`available_quantity`).
  - **Xử lý Race Condition / Overselling**:
    - Áp dụng **Pessimistic Locking** (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) hoặc **Optimistic Locking** (`@Version`) khi trừ kho số lượng vé.
  - Quản lý Transaction phân tán / `@Transactional` bảo đảm tính nhất quán dữ liệu.
- [ ] Cơ chế giữ chỗ tạm thời (Hold Ticket) và hủy đơn quá hạn thanh toán (Scheduled Job / Expiration).

---

### Giai Đoạn 6: Tối Ưu Hiệu Năng & Nâng Cao (Caching, Async, Notifications)

- [ ] **Redis Caching**:
  - Cache danh sách sự kiện hot, chi tiết sự kiện và danh mục để giảm tải truy vấn MySQL.
  - Chiến lược invalidate cache khi sự kiện cập nhật (`@CacheEvict`).
- [ ] **Email Thông Báo Bất Đồng Bộ**:
  - Gửi email xác nhận đặt vé kèm mã vé (sử dụng `@Async` hoặc Message Queue RabbitMQ/Kafka).
- [ ] **Check-in sự kiện**:
  - API soát vé bằng mã vé / QR Code dành cho Organizer tại cửa check-in.
- [ ] **Database Migration với Flyway**:
  - Quản lý phiên bản database schema bằng SQL migrations thay vì Hibernate ddl-auto.

---

## 📝 Ghi Chú & Ý Tưởng Bổ Sung Của Bạn

*(Bạn có thể tự do bổ sung các yêu cầu nghiệp vụ hoặc tính năng riêng của mình vào mục này)*

1.
2.
3.

---

## 📚 Kỹ Năng & Câu Hỏi Phỏng Vấn Cần Nắm Theo Từng Module

- **Security**: Stateless JWT vs Stateful Session; Phân biệt Authentication vs Authorization; Cách phòng chống lỗ hổng IDOR.
- **JPA & Hibernate**: Lazy vs Eager Loading; Lỗi N+1 Query và cách giải quyết (`JOIN FETCH`, `EntityGraph`); MappedSuperclass vs SingleTable.
- **Concurrency**: Race Condition là gì? Phân biệt Pessimistic Locking và Optimistic Locking; Khi nào dùng cái nào?
- **Database Indexing**: Chọn cột nào để đánh index? B-Tree index hoạt động như thế nào trong MySQL?
