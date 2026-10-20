# EventHub Backend

> Base project Backend sản xuất (Production-oriented) cho hệ thống quản lý sự kiện **EventHub**, được xây dựng trên nền tảng **Java 21** và **Spring Boot 3**.

---

## 🛠 Công Nghệ Sử Dụng

- **Ngôn ngữ**: Java 21 LTS
- **Framework**: Spring Boot 3.3.4
- **Build Tool**: Maven (kèm Maven Wrapper)
- **Database**: MySQL 8.4 LTS (hỗ trợ H2 cho automated tests)
- **ORM**: Spring Data JPA & Hibernate
- **Security**: Spring Security 6 & JWT (HMAC-SHA256)
- **Validation**: Jakarta Bean Validation (`@Valid`, `@NotBlank`, ...)
- **Tài liệu API**: Springdoc OpenAPI / Swagger UI 3
- **Containerization**: Docker & Docker Compose
- **Testing**: JUnit 5, Mockito, AssertJ, Spring Security Test

---

## 📁 Cấu Trúc Thư Mục Chuẩn

```
EventHub/
├── .env.example                     # Mẫu biến môi trường
├── Dockerfile                       # Multi-stage Docker build cho Java 21
├── docker-compose.yml               # Cấu hình container MySQL & Backend
├── pom.xml                          # Maven POM quản lý thư viện và plugin
├── README.md                        # Hướng dẫn dự án
├── docker/
│   └── mysql/
│       └── init.sql                 # Script khởi tạo database MySQL
├── docs/
│   ├── architecture.md              # Tài liệu phân tích kiến trúc chi tiết
│   └── api-spec.md                  # Đặc tả danh sách API và payload mẫu
├── src/
│   ├── main/
│   │   ├── java/com/eventhub/
│   │   │   ├── EventHubApplication.java   # Main class
│   │   │   ├── common/                    # ApiResponse<T>, PageResponse<T>
│   │   │   ├── config/                    # CorsConfig, JpaAuditingConfig, OpenApiConfig
│   │   │   ├── controllers/               # EventController, AuthController
│   │   │   ├── dto/
│   │   │   │   ├── request/               # Create/Update EventRequest, Login/RegisterRequest
│   │   │   │   └── response/              # EventResponse, AuthResponse
│   │   │   ├── entities/                  # BaseEntity, Event, User
│   │   │   ├── enums/                     # EventStatus, UserRole
│   │   │   ├── events/                    # EventCreatedEvent (Domain Event)
│   │   │   ├── exceptions/                # AppException, GlobalExceptionHandler, ErrorCode
│   │   │   ├── jobs/                      # SampleScheduledJob (@Scheduled)
│   │   │   ├── listeners/                 # EventNotificationListener (@EventListener)
│   │   │   ├── mappers/                   # EventMapper (DTO <-> Entity)
│   │   │   ├── repositories/              # EventRepository, UserRepository
│   │   │   ├── security/                  # JwtService, JwtFilter, SecurityConfig
│   │   │   ├── services/                  # EventService, AuthService
│   │   │   │   └── impl/                  # EventServiceImpl
│   │   │   └── utils/                     # DateUtils
│   │   └── resources/
│   │       ├── application.yml            # Cấu hình chung
│   │       ├── application-dev.yml        # Profile phát triển (dev)
│   │       └── application-prod.yml       # Profile sản xuất (prod)
│   └── test/
│       ├── java/com/eventhub/
│       │   ├── EventHubApplicationTests.java
│       │   ├── controllers/EventControllerTest.java
│       │   └── services/EventServiceTest.java
│       └── resources/
│           └── application-test.yml       # Profile test (H2 in-memory)
```

---

## 🚀 Hướng Dẫn Cài Đặt & Khởi Chạy

### 1. Chuẩn bị môi trường
- **JDK 21** (hoặc mới hơn)
- **Docker & Docker Compose** (hoặc cài đặt MySQL cục bộ)

### 2. Thiết lập cấu hình môi trường
Tạo file `.env` từ file mẫu:
```bash
cp .env.example .env
```
Hoặc chỉnh sửa các tham số trong `src/main/resources/application-dev.yml` phù hợp với máy của bạn.

---

### 3. Cách 1: Chạy bằng Docker Compose (Khuyên dùng)
Toàn bộ hệ thống (MySQL + Spring Boot Backend) sẽ được build và khởi động chỉ với 1 lệnh:
```bash
docker compose up -d --build
```
Kiểm tra logs:
```bash
docker compose logs -f backend
```
Dừng hệ thống:
```bash
docker compose down
```

---

### 4. Cách 2: Chạy trực tiếp từ dòng lệnh (Maven)

#### Bước 4.1: Khởi động MySQL (hoặc dùng container MySQL độc lập)
```bash
docker run --name eventhub-mysql -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=eventhub_db -p 3306:3306 -d mysql:8.4
```

#### Bước 4.2: Chạy ứng dụng bằng Maven Wrapper
Trên Windows PowerShell/CMD:
```powershell
.\mvnw.cmd spring-boot:run
```
Trên Linux/macOS:
```bash
./mvnw spring-boot:run
```

---

## 📖 Tài Liệu Swagger & OpenAPI

Khi ứng dụng chạy, truy cập giao diện tương tác Swagger UI tại:
👉 **[http://localhost:8088/swagger-ui.html](http://localhost:8088/swagger-ui.html)**

Swagger UI đã được tích hợp nút **Authorize (Bearer Token)** để bạn có thể test trực tiếp các endpoint yêu cầu xác thực JWT.

---

## 🔑 Tài Khoản Demo Mặc Định

Khi khởi động lần đầu, hệ thống tự động sinh 2 tài khoản mẫu (nếu database chưa có user):
1. **Admin**:
   - Username: `admin`
   - Password: `admin123`
   - Role: `ROLE_ADMIN`
2. **User**:
   - Username: `user`
   - Password: `user123`
   - Role: `ROLE_USER`

---

## 🧪 Chạy Kiểm Thử (Unit & Integration Tests)

Hệ thống kiểm thử sử dụng profile `test` với database H2 in-memory, hoàn toàn độc lập và không phụ thuộc vào MySQL cục bộ:
```powershell
.\mvnw.cmd test
```

---

## 📡 Danh Sách API Chính (Cheatsheet)

### 1. Authentication
| Phương thức | Endpoint | Mô tả | Quyền |
|---|---|---|---|
| `POST` | `/api/v1/auth/login` | Đăng nhập nhận JWT token | Public |
| `POST` | `/api/v1/auth/register` | Đăng ký tài khoản mới | Public |

### 2. Events (Module mẫu)
| Phương thức | Endpoint | Mô tả | Quyền |
|---|---|---|---|
| `GET` | `/api/v1/events` | Danh sách sự kiện (tìm kiếm + phân trang) | Public |
| `GET` | `/api/v1/events/{id}` | Chi tiết sự kiện | Public |
| `POST` | `/api/v1/events` | Tạo sự kiện mới | Cần Token Bearer |
| `PUT` | `/api/v1/events/{id}` | Cập nhật thông tin sự kiện | Cần Token Bearer |
| `DELETE` | `/api/v1/events/{id}` | Xóa sự kiện | Cần Token Bearer |
