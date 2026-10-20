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
