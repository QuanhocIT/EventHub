# Tài Liệu Đặc Tả API (EventHub API Specification)

Tất cả các API trả về theo cấu trúc chuẩn:

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "errors": null,
  "timestamp": "2026-09-25T17:30:00"
}
```

---

## 1. Authentication APIs

### 1.1 Đăng ký tài khoản

- **Endpoint**: `POST /api/v1/auth/register`
- **Quyền**: Public
- **Request Body**:

```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "Password123!",
  "fullName": "John Doe"
}
```

- **Response**: `201 Created`

### 1.2 Đăng nhập

- **Endpoint**: `POST /api/v1/auth/login`
- **Quyền**: Public
- **Request Body**:

```json
{
  "usernameOrEmail": "admin",
  "password": "admin123"
}
```

- **Response**: `200 OK`

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "username": "admin",
    "email": "admin@eventhub.com",
    "role": "ROLE_ADMIN"
  }
}
```

---

## 2. Event APIs

### 2.1 Lấy danh sách sự kiện (Phân trang, Tìm kiếm & Lọc Đa Tiêu Chí)

- **Endpoint**: `GET /api/v1/events`
- **Quyền**: Public
- **Query Parameters**:
  - `keyword` (String, optional): Tìm kiếm theo title, description hoặc location
  - `categoryId` (Long, optional): Lọc theo ID danh mục sự kiện
  - `status` (Enum, optional): `DRAFT`, `PUBLISHED`, `CANCELLED`, `COMPLETED`
  - `minPrice` (BigDecimal, optional): Giá vé tối thiểu
  - `maxPrice` (BigDecimal, optional): Giá vé tối đa
  - `startDate` (ISO DateTime, optional): Sự kiện diễn ra từ ngày (e.g. `2026-11-01T00:00:00`)
  - `endDate` (ISO DateTime, optional): Sự kiện kết thúc trước ngày (e.g. `2026-12-31T23:59:59`)
  - `location` (String, optional): Địa điểm / thành phố (Hanoi, TP.HCM, ...)
  - `page` (Integer, default 0)
  - `size` (Integer, default 10)
  - `sortBy` (String, default "createdAt")
  - `direction` (String: "asc" / "desc", default "desc")
- **Response**: `200 OK`

### 2.2 Xem chi tiết sự kiện

- **Endpoint**: `GET /api/v1/events/{id}`
- **Quyền**: Public
- **Response**: `200 OK` (chứa `categoryId`, `categoryName`, `categorySlug`, `organizerId`, ...)

### 2.3 Tạo sự kiện mới

- **Endpoint**: `POST /api/v1/events`
- **Quyền**: Yêu cầu xác thực (`ROLE_ORGANIZER` hoặc `ROLE_ADMIN`)
- **Request Body**:

```json
{
  "title": "Vietnam Tech Summit 2026",
  "description": "Hội nghị công nghệ thường niên quy mô lớn",
  "location": "Trung tâm Hội nghị Quốc gia, Hà Nội",
  "startTime": "2026-11-20T08:30:00",
  "endTime": "2026-11-20T17:30:00",
  "capacity": 1000,
  "price": 499000.00,
  "status": "PUBLISHED",
  "categoryId": 1
}
```

- **Response**: `201 Created`

### 2.4 Cập nhật sự kiện

- **Endpoint**: `PUT /api/v1/events/{id}`
- **Quyền**: Yêu cầu xác thực (Owner hoặc `ROLE_ADMIN`)
- **Response**: `200 OK`

### 2.5 Xóa sự kiện

- **Endpoint**: `DELETE /api/v1/events/{id}`
- **Quyền**: Yêu cầu xác thực (Owner hoặc `ROLE_ADMIN`)
- **Response**: `200 OK`

---

## 3. Category APIs (Danh Mục Sự Kiện)

### 3.1 Lấy danh sách danh mục hoạt động

- **Endpoint**: `GET /api/v1/categories`
- **Quyền**: Public

### 3.2 Lấy chi tiết danh mục theo ID hoặc Slug

- **Endpoint**: `GET /api/v1/categories/{id}` hoặc `GET /api/v1/categories/slug/{slug}`
- **Quyền**: Public

### 3.3 Tạo danh mục mới

- **Endpoint**: `POST /api/v1/categories`
- **Quyền**: `ROLE_ADMIN` hoặc `ROLE_ORGANIZER`
- **Request Body**:

```json
{
  "name": "Công nghệ & Hội thảo",
  "description": "Diễn đàn công nghệ, Trí tuệ nhân tạo và Blockchain",
  "iconUrl": "https://example.com/icons/tech.png"
}
```

### 3.4 Cập nhật / Xóa danh mục

- **Endpoint**: `PUT /api/v1/categories/{id}`, `DELETE /api/v1/categories/{id}`
- **Quyền**: `ROLE_ADMIN`

---

## 4. Coupon & Voucher APIs (Khuyến Mãi & Giảm Giá)

### 4.1 Kiểm tra & Tính toán giảm giá trước khi đặt vé

- **Endpoint**: `POST /api/v1/coupons/validate`
- **Quyền**: Yêu cầu xác thực Bearer token
- **Request Body**:

```json
{
  "code": "CHAOMUNG2026",
  "eventId": 1,
  "orderAmount": 500000.00
}
```

- **Response**: `200 OK`

```json
{
  "success": true,
  "message": "Coupon verified successfully",
  "data": {
    "code": "CHAOMUNG2026",
    "valid": true,
    "discountAmount": 50000.00,
    "finalAmount": 450000.00,
    "message": "Coupon applied: 10% off (saved 50000)"
  }
}
```

### 4.2 Tạo mã giảm giá mới

- **Endpoint**: `POST /api/v1/coupons`
- **Quyền**: `ROLE_ADMIN` hoặc `ROLE_ORGANIZER`
- **Request Body**:

```json
{
  "code": "EARLYBIRD15",
  "description": "Giảm 15% tối đa 150.000đ cho đơn từ 200.000đ",
  "discountType": "PERCENTAGE",
  "discountValue": 15.0,
  "maxDiscountAmount": 150000.0,
  "minOrderAmount": 200000.0,
  "usageLimit": 200,
  "startDate": "2026-10-01T00:00:00",
  "endDate": "2026-12-31T23:59:59",
  "eventId": null
}
```

### 4.3 Xem danh sách và Xóa mã giảm giá

- **Endpoint**: `GET /api/v1/coupons`, `DELETE /api/v1/coupons/{id}`
- **Quyền**: `ROLE_ADMIN` hoặc `ROLE_ORGANIZER`

---

## 5. User Profile & Admin Management APIs

### 5.1 Xem hồ sơ người dùng hiện tại

- **Endpoint**: `GET /api/v1/users/me`
- **Quyền**: Yêu cầu xác thực Bearer token (mọi role)
- **Response**: `200 OK`

```json
{
  "success": true,
  "message": "User profile retrieved successfully",
  "data": {
    "id": 1,
    "username": "customer",
    "email": "customer@eventhub.com",
    "fullName": "Nguyen Van A",
    "phoneNumber": "0987654321",
    "avatarUrl": "https://images.unsplash.com/photo-1534528741775-53994a69daeb",
    "role": "ROLE_CUSTOMER",
    "enabled": true,
    "createdAt": "2026-10-21T09:00:00"
  }
}
```

### 5.2 Cập nhật hồ sơ cá nhân

- **Endpoint**: `PUT /api/v1/users/me`
- **Quyền**: Yêu cầu xác thực Bearer token (mọi role)
- **Request Body**:

```json
{
  "fullName": "Nguyen Van A Updated",
  "phoneNumber": "0912345678",
  "avatarUrl": "https://images.unsplash.com/photo-1534528741775-53994a69daeb"
}
```

- **Response**: `200 OK`

### 5.3 Đổi mật khẩu

- **Endpoint**: `PUT /api/v1/users/me/change-password`
- **Quyền**: Yêu cầu xác thực Bearer token (mọi role)
- **Request Body**:

```json
{
  "currentPassword": "customer123",
  "newPassword": "NewSecurePassword456!",
  "confirmPassword": "NewSecurePassword456!"
}
```

- **Response**: `200 OK`

### 5.4 Admin: Danh sách người dùng (Tìm kiếm, Lọc & Phân trang)

- **Endpoint**: `GET /api/v1/users`
- **Quyền**: `ROLE_ADMIN`
- **Query Parameters**:
  - `keyword` (String, optional): Tìm kiếm theo username, email, họ tên hoặc SĐT
  - `role` (Enum, optional): `ROLE_CUSTOMER`, `ROLE_ORGANIZER`, `ROLE_STAFF`, `ROLE_ADMIN`
  - `enabled` (Boolean, optional): `true` hoặc `false`
  - `page` (Integer, default 0)
  - `size` (Integer, default 10)
  - `sortBy` (String, default "createdAt")
  - `direction` (String: "asc" / "desc", default "desc")
- **Response**: `200 OK` (phân trang `PageResponse<UserProfileResponse>`)

### 5.5 Admin: Khóa hoặc Kích hoạt tài khoản

- **Endpoint**: `PATCH /api/v1/users/{id}/status`
- **Quyền**: `ROLE_ADMIN` (Ngăn chặn Admin tự khóa tài khoản của mình)
- **Request Body**:

```json
{
  "enabled": false
}
```

- **Response**: `200 OK`

### 5.6 Admin: Phân quyền vai trò người dùng

- **Endpoint**: `PATCH /api/v1/users/{id}/role`
- **Quyền**: `ROLE_ADMIN` (Ngăn chặn Admin tự hạ quyền của mình)
- **Request Body**:

```json
{
  "role": "ROLE_ORGANIZER"
}
```

- **Response**: `200 OK`

---

## 6. Review & Rating APIs

### 6.1 Lấy danh sách đánh giá của sự kiện

- **Endpoint**: `GET /api/v1/events/{eventId}/reviews`
- **Quyền**: Public
- **Query Parameters**:
  - `page` (Integer, default 0)
  - `size` (Integer, default 10)
- **Response**: `200 OK` (`PageResponse<ReviewResponse>`)

### 6.2 Báo cáo tổng quan xếp hạng & phân bổ số sao

- **Endpoint**: `GET /api/v1/events/{eventId}/reviews/summary`
- **Quyền**: Public
- **Response**: `200 OK`

```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    "eventId": 1,
    "averageRating": 4.85,
    "totalReviews": 128,
    "star5Count": 110,
    "star4Count": 14,
    "star3Count": 3,
    "star2Count": 1,
    "star1Count": 0
  }
}
```

### 6.3 Lấy đánh giá của chính người dùng hiện tại

- **Endpoint**: `GET /api/v1/events/{eventId}/reviews/my-review`
- **Quyền**: Authenticated (`ROLE_CUSTOMER`, `ROLE_ORGANIZER`, `ROLE_ADMIN`)
- **Response**: `200 OK` (Trả về `ReviewResponse` hoặc `null` nếu chưa đánh giá)

### 6.4 Gửi đánh giá sự kiện (Verified Attendee Guard)

- **Endpoint**: `POST /api/v1/events/{eventId}/reviews`
- **Quyền**: Authenticated
- **Điều kiện nghiệp vụ**:
  - Sự kiện đã bắt đầu diễn ra (`startTime <= now`).
  - Người dùng có đơn hàng xác nhận (`CONFIRMED`) hoặc vé đã check-in (`USED`).
  - Mỗi người dùng chỉ được đánh giá 1 lần (dùng PUT để sửa).
- **Request Body**:

```json
{
  "rating": 5,
  "comment": "Sự kiện được chuẩn bị chu đáo, diễn giả chất lượng và hệ thống check-in vé QR mượt mà!"
}
```

- **Response**: `201 Created`

### 6.5 Cập nhật đánh giá của mình

- **Endpoint**: `PUT /api/v1/reviews/{reviewId}`
- **Quyền**: Authenticated (Chỉ tác giả đánh giá)
- **Request Body**:

```json
{
  "rating": 4,
  "comment": "Bổ sung nhận xét sau khi nhận được tài liệu slide và recording."
}
```

- **Response**: `200 OK`

### 6.6 Xóa đánh giá

- **Endpoint**: `DELETE /api/v1/reviews/{reviewId}`
- **Quyền**: Tác giả đánh giá hoặc `ROLE_ADMIN`
- **Response**: `200 OK`
