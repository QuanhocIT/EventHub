# Đặc Tả Kỹ Thuật Dự Án EventHub (System Specification)

> **Hệ Thống Quản Lý & Bán Vé Sự Kiện Trực Tuyến Chịu Tải Cao (Event Ticketing Platform)**  
> **Ngôn ngữ**: Java 21 LTS | **Framework**: Spring Boot 3.3.4 | **Cơ sở dữ liệu**: MySQL 8.4 LTS, Redis 7 Alpine

---

## 1. Tầm Nhìn & Mục Tiêu Dự Án

EventHub là nền tảng phân phối vé điện tử và quản lý sự kiện trực tuyến toàn diện, giải quyết các thách thức công nghệ cốt lõi trong ngành ticketing:
1. **Chống Overselling & Race Condition**: Ngăn chặn tình trạng 2 người cùng mua thành công 1 ghế hoặc 1 vé khi mở bán số lượng lớn.
2. **Quản lý Ghế & Bản Đồ Địa Điểm (Seat Map)**: Hỗ trợ sơ đồ phân khu (VIP, Standard, Balcony) và cơ chế giữ chỗ tạm thời (Temporary Hold) thời gian thực.
3. **Thanh Toán Đảm Bảo Toàn Vẹn (Idempotency)**: Đảm bảo giao dịch ngân hàng / sandbox an toàn, chống double-charging khi mạng lag.
4. **Vé Điện Tử & Chống Gian Lận (Anti-Passback)**: Phát hành vé mã QR Base64 mã hóa, kiểm soát soát vé cổng không cho phép quét lại lần 2.
5. **Hệ Thống Khuyến Mãi (Coupons/Vouchers)**: Kiểm soát số lượng sử dụng chính xác bằng Pessimistic Write Lock và cơ chế hoàn mã khi đơn bị hủy.
6. **Báo Cáo Doanh Thu Thời Gian Thực**: Tối ưu truy vấn báo cáo tài chính bằng JPQL Aggregations tránh N+1.

---

## 2. Mô Hình Phân Quyền (RBAC) & Vai Trò Hệ Thống

| Vai trò (Role) | Trách nhiệm & Quyền hạn |
|---|---|
| `ROLE_CUSTOMER` | Xem sự kiện, tìm kiếm lọc nâng cao, tạm giữ ghế, áp mã giảm giá, đặt vé, thanh toán sandbox và xem vé điện tử cá nhân. |
| `ROLE_ORGANIZER` | Tạo và quản lý sự kiện của mình, quản lý hạng vé (TicketType), tạo sơ đồ ghế địa điểm, tạo mã giảm giá cho sự kiện, xem Dashboard doanh thu. |
| `ROLE_STAFF` | Quét mã vé QR Code tại cổng check-in (`POST /api/v1/tickets/{code}/check-in`), kiểm tra tính hợp lệ và chống quay vòng vé (Anti-passback). |
| `ROLE_ADMIN` | Toàn quyền kiểm soát hệ thống: quản lý danh mục (Category), quản lý toàn bộ sự kiện, người dùng, xem Dashboard toàn sàn. |

---

## 3. Danh Sách Các Thực Thể Chính (Domain Entities)

1. **User**: Thông tin tài khoản, mật khẩu mã hóa BCrypt, vai trò `UserRole`.
2. **Category**: Phân loại sự kiện (Âm nhạc, Công nghệ, Thể thao,...), slug SEO-friendly.
3. **Venue**: Địa điểm tổ chức (Tên, địa chỉ, sức chứa).
4. **Seat**: Ghế vật lý tại địa điểm (Dãy hàng, số ghế, phân khu VIP/Standard/Balcony).
5. **Event**: Sự kiện (Tên, mô tả, thời gian bắt đầu/kết thúc, giá sàn, địa điểm, danh mục, organizer).
6. **EventSeat**: Bản sao trạng thái ghế theo từng sự kiện (`AVAILABLE`, `HELD`, `SOLD`, `BLOCKED`).
7. **TicketType**: Các hạng vé sự kiện (Early Bird, Standard, VIP Pass, số lượng có hạn, min/max per order).
8. **Coupon**: Mã giảm giá khuyến mãi (`PERCENTAGE` hoặc `FIXED_AMOUNT`, min order, quota lượt dùng, locking).
9. **Booking**: Đơn đặt vé (`EVH-YYYYMMDD-XXXX`), lưu subtotal, discount, coupon code, total amount, hạn thanh toán.
10. **BookingItem**: Chi tiết từng hạng vé hoặc ghế trong đơn hàng.
11. **Payment**: Giao dịch thanh toán với Idempotency Key, provider (`MOCK_SANDBOX`), transaction code.
12. **Ticket**: Vé điện tử có mã định danh duy nhất, chuỗi QR Code Base64, trạng thái `VALID`, `USED`, `CANCELLED`.

---

## 4. Các Quy Trình Nghiệp Vụ Cốt Lõi (Business Workflows)

### 4.1 Quy trình Tìm kiếm & Lọc sự kiện nâng cao
- Hỗ trợ tìm kiếm theo từ khóa (tiêu đề, địa điểm, nội dung).
- Lọc động theo ID danh mục, trạng thái, khoảng giá (`minPrice`, `maxPrice`), khoảng thời gian (`startDate`, `endDate`).
- Tích hợp Spring Data JPA Specification (`EventSpecification`) đảm bảo an toàn truy vấn và phân trang chuẩn.

### 4.2 Quy trình Đặt vé & Áp dụng Mã giảm giá
1. Khách hàng chọn hạng vé hoặc ghế mong muốn.
2. Hệ thống kiểm tra số lượng tồn kho với **Pessimistic Lock** (`findByIdAndEventIdWithLock`).
3. Nếu có mã giảm giá (`couponCode`), gọi `CouponService.applyCouponAndIncrementUsage` với `@Lock(PESSIMISTIC_WRITE)` để kiểm tra tính hợp lệ và tăng biến đếm sử dụng an toàn tuyệt đối.
4. Tạo đơn `Booking` trạng thái `PENDING_PAYMENT`, thời gian hết hạn sau 15 phút.
5. Nếu đơn hàng quá hạn thanh toán hoặc người dùng ấn hủy đơn (`cancelBooking`):
   - Hoàn trả số lượng vé về kho tồn.
   - Giải phóng trạng thái ghế về `AVAILABLE`.
   - Hoàn trả lượt dùng mã coupon (`releaseCouponUsage`).

### 4.3 Quy trình Thanh toán & Xuất vé
1. Khách hàng gửi yêu cầu thanh toán kèm `Idempotency-Key`.
2. Hệ thống kiểm tra: nếu key đã xử lý thành công thì trả lại kết quả ngay, không trừ tiền 2 lần.
3. Khi thanh toán thành công:
   - Chuyển `Booking` sang `CONFIRMED`.
   - Chuyển `Payment` sang `SUCCESS`.
   - Sinh vé điện tử `Ticket` kèm mã QR Code Base64 độ phân giải cao qua Google ZXing.
   - Bắn Domain Event `BookingConfirmedEvent` kích hoạt `@Async` gửi email thông báo.

### 4.4 Quy trình Soát vé tại cổng (Check-in Gate)
1. Nhân viên `ROLE_STAFF` quét mã QR hoặc nhập `ticketCode`.
2. Hệ thống kiểm tra:
   - Nếu vé không tồn tại hoặc không thuộc sự kiện: từ chối (`ERR_404`).
   - Nếu vé đã `USED`: báo lỗi đã quét lúc mấy giờ bởi nhân viên nào (chống gian lận quay vòng vé).
   - Nếu vé hợp lệ: cập nhật `status = USED`, ghi nhận `checkedInAt` và `checkedInBy`.
