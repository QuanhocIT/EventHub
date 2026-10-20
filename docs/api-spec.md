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

## 2. Event APIs (Module mẫu)

### 2.1 Lấy danh sách sự kiện (Phân trang & Tìm kiếm)
- **Endpoint**: `GET /api/v1/events`
- **Quyền**: Public
- **Query Parameters**:
  - `keyword` (String, optional): Tìm kiếm theo title hoặc location
  - `status` (Enum, optional): `DRAFT`, `PUBLISHED`, `CANCELLED`, `COMPLETED`
  - `page` (Integer, default 0)
  - `size` (Integer, default 10)
  - `sortBy` (String, default "createdAt")
  - `direction` (String: "asc" / "desc", default "desc")
- **Response**: `200 OK`

### 2.2 Xem chi tiết sự kiện
- **Endpoint**: `GET /api/v1/events/{id}`
- **Quyền**: Public
- **Response**: `200 OK`

### 2.3 Tạo sự kiện mới
- **Endpoint**: `POST /api/v1/events`
- **Quyền**: Yêu cầu xác thực (`Authorization: Bearer <token>`)
- **Request Body**:
```json
{
  "title": "Vietnam Tech Summit 2026",
  "description": "Hội nghị công nghệ thường niên quy mô lớn",
  "location": "Trung tâm Hội nghị Quốc gia, Hà Nội",
  "startTime": "2026-11-20T08:30:00",
  "endTime": "2026-11-20T17:30:00",
  "capacity": 1000,
  "price": 199.00,
  "status": "PUBLISHED"
}
```
- **Response**: `201 Created`

### 2.4 Cập nhật sự kiện
- **Endpoint**: `PUT /api/v1/events/{id}`
- **Quyền**: Yêu cầu xác thực (`Authorization: Bearer <token>`)
- **Response**: `200 OK`

### 2.5 Xóa sự kiện
- **Endpoint**: `DELETE /api/v1/events/{id}`
- **Quyền**: Yêu cầu xác thực (`Authorization: Bearer <token>`)
- **Response**: `200 OK`
