# Kiến Trúc Hệ Thống EventHub Backend

## 1. Tổng quan thiết kế

EventHub Backend được xây dựng theo kiến trúc **Layered Architecture (Phân lớp)** tiêu chuẩn của Spring Boot, hướng tới môi trường Production với các nguyên tắc:
- **Separation of Concerns (SoC)**: Mỗi tầng chỉ chịu trách nhiệm duy nhất cho miền của mình.
- **Data Transfer Objects (DTO)**: Tuyệt đối không expose Entity trực tiếp ra ngoài API.
- **Thin Controllers, Rich Services**: Controller chỉ mapping HTTP và validation cú pháp; toàn bộ business logic và điều phối nằm ở Service.
- **Fail-Fast & Unified Response**: Mọi response (thành công hay thất bại) đều tuân thủ cấu trúc `ApiResponse<T>`.

---

## 2. Sơ đồ các tầng kiến trúc

```
[ Client / Web / Mobile / Swagger ]
                 │
                 ▼  (HTTP REST / JSON)
        [ Security & JWT Filter ]
                 │
                 ▼
        [ Controller Layer ]  <─── DTO Validation (@Valid)
                 │
                 ▼  (DTO)
         [ Service Layer ]    <─── Business Logic, Transactions, Events
          │            │
          │            ▼
          │     [ Event Publisher ] ──> [ Event Listener ] (Async/Domain Events)
          ▼
       [ Mapper ]             <─── DTO <-> Entity
          │
          ▼
     [ Repository ]           <─── Spring Data JPA
          │
          ▼
    [ Database / MySQL ]
```

---

## 3. Chi tiết các package trong `src/main/java/com/eventhub/`

| Package | Mục đích & Trách nhiệm |
|---|---|
| `common/` | Các wrapper dùng chung toàn hệ thống (`ApiResponse<T>`, `PageResponse<T>`). |
| `config/` | Cấu hình Spring (`CorsConfig`, `JpaAuditingConfig`, `OpenApiConfig`). |
| `controllers/` | Tiếp nhận HTTP request, validate DTO, gọi Service, trả về HTTP status & ApiResponse. |
| `services/` | Khai báo interface và các implement (`services/impl/`) chứa toàn bộ nghiệp vụ. |
| `repositories/`| Giao tiếp database thông qua Spring Data JPA. Không chứa logic nghiệp vụ. |
| `entities/` | JPA Entities phản chiếu schema database. Kế thừa `BaseEntity` (id, createdAt, updatedAt). |
| `dto/` | Chứa Request DTO (`dto/request/`) và Response DTO (`dto/response/`). |
| `exceptions/` | Custom Exceptions (`AppException`, `ResourceNotFoundException`, ...) và `GlobalExceptionHandler`. |
| `security/` | Cấu hình bảo mật Spring Security 6, JWT Filter, Token Provider, UserDetailsService. |
| `enums/` | Các hằng số định danh nghiệp vụ (`EventStatus`, `UserRole`). |
| `mappers/` | Chuyển đổi dữ liệu giữa Entity và DTO. |
| `events/` | Khai báo Domain Events (ví dụ: `EventCreatedEvent`). |
| `listeners/` | Lắng nghe và xử lý sự kiện bất đồng bộ hoặc tách biệt module (`EventNotificationListener`). |
| `jobs/` | Các tác vụ định kỳ background (@Scheduled). |
| `utils/` | Các hàm tiện ích thuần túy (ví dụ: `DateUtils`). |

---

## 4. Bảo mật & Xác thực (Spring Security + JWT)

- **Cơ chế**: Stateless Authentication sử dụng Bearer JWT token.
- **Quy trình**:
  1. Client gửi credentials tới `/api/v1/auth/login`.
  2. `AuthService` xác thực thông qua `AuthenticationManager`.
  3. `JwtService` sinh JWT token chứa username và role.
  4. Client đính kèm Header: `Authorization: Bearer <token>` vào các request được bảo vệ.
  5. `JwtAuthenticationFilter` phân tích token và nạp `Authentication` vào `SecurityContextHolder`.

---

## 5. Lộ trình tích hợp nâng cao (Tiếp theo)

1. **Redis Caching**: Caching danh sách sự kiện hot, kiểm tra trùng lặp và rate limiting.
2. **Message Queue (RabbitMQ / Kafka)**: Tách việc gửi email xác nhận đặt vé thành hàng đợi.
3. **Database Migration (Flyway/Liquibase)**: Quản lý version schema thay cho Hibernate auto ddl.
