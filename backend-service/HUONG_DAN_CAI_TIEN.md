# 📖 HƯỚNG DẪN SỬ DỤNG CÁC CẢI TIẾN MỚI

---

## 🎯 MỤC ĐÍCH

Tài liệu này hướng dẫn cách sử dụng và tận dụng các cải tiến mới được thêm vào dự án Backend Service.

---

## 🚀 QUICK START

### Bước 1: Cấu hình Environment Variables

```bash
# 1. Copy file mẫu
cp env.example .env

# 2. Tạo JWT keys (chọn một trong các cách sau)

# Linux/Mac:
export JWT_ACCESS_KEY=$(openssl rand -base64 64)
export JWT_REFRESH_KEY=$(openssl rand -base64 64)

# Windows PowerShell:
$JWT_ACCESS_KEY = [Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }))
$JWT_REFRESH_KEY = [Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Maximum 256 }))

# 3. Chỉnh sửa .env với thông tin của bạn
DB_PASSWORD=your-secure-password
SENDGRID_API_KEY=your-api-key
```

### Bước 2: Chạy Application

**Option A: Với Docker (Khuyên dùng)**
```bash
docker-compose up --build -d
docker-compose logs -f backend-service
```

**Option B: Local Development**
```bash
# Đảm bảo MySQL đang chạy
mvn spring-boot:run
```

### Bước 3: Verify

```bash
# Kiểm tra health
curl http://localhost:8081/actuator/health

# Truy cập Swagger UI
open http://localhost:8081/swagger-ui.html
```

---

## 📚 CHI TIẾT CÁC CẢI TIẾN

### 1. 🔐 PASSWORD VALIDATION

#### Sử dụng @ValidPassword Annotation

**Trước đây:**
```java
public class UserRequest {
    @Size(min = 8, message = "Password phải có ít nhất 8 ký tự")
    private String password;
}
```

**Bây giờ:**
```java
public class UserRequest {
    @ValidPassword  // Tự động validate tất cả yêu cầu
    private String password;
}
```

#### Yêu cầu Password Mới

Mật khẩu phải có:
- ✅ Tối thiểu 8 ký tự
- ✅ Ít nhất 1 chữ HOA (A-Z)
- ✅ Ít nhất 1 chữ thường (a-z)
- ✅ Ít nhất 1 số (0-9)
- ✅ Ít nhất 1 ký tự đặc biệt (!@#$%^&*...)

#### Ví dụ

```bash
# Valid passwords ✅
"MyPass123!"
"SecureP@ssw0rd"
"Admin#2025xyz"

# Invalid passwords ❌
"password"      # Thiếu HOA, số, ký tự đặc biệt
"PASSWORD123"   # Thiếu thường, ký tự đặc biệt
"Pass123"       # Quá ngắn (< 8)
```

#### Test API

```bash
# Test với password hợp lệ
curl -X POST http://localhost:8081/user/add \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "ValidPass123!",
    "email": "test@example.com"
  }'

# Response: 201 Created

# Test với password không hợp lệ
curl -X POST http://localhost:8081/user/add \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "weak",
    "email": "test@example.com"
  }'

# Response: 400 Bad Request
{
  "status": 400,
  "message": "Validation error: Invalid input data",
  "errors": {
    "password": "Password must be at least 8 characters long"
  }
}
```

---

### 2. 📝 REQUEST/RESPONSE LOGGING

#### Tính năng

Tất cả HTTP requests tự động được log với thông tin:
- Request: Method, URI, Client IP, User-Agent
- Response: Status code, Execution time
- Warning cho slow requests (> 1 second)
- Chi tiết lỗi cho 4xx, 5xx responses

#### Ví dụ Logs

```log
2025-10-21 10:30:15 - → Incoming Request: POST /user/add from 192.168.1.100 | User-Agent: PostmanRuntime/7.32.3
2025-10-21 10:30:15 - ← Outgoing Response: 201 ✓ Success | Execution Time: 45ms

2025-10-21 10:31:20 - → Incoming Request: GET /user/list from 192.168.1.100 | User-Agent: Mozilla/5.0
2025-10-21 10:31:22 - ⚠️ Slow Request Detected: 1250ms
2025-10-21 10:31:22 - ← Outgoing Response: 200 ✓ Success | Execution Time: 1250ms
```

#### Xem Logs

```bash
# Docker logs
docker-compose logs -f backend-service

# Application logs (production)
tail -f /var/log/backend-service/application.log

# Filter for errors only
docker-compose logs backend-service | grep "✗ Server Error"
```

---

### 3. ⚠️ ENHANCED ERROR HANDLING

#### Các Lỗi Mới Được Xử Lý

**1. Duplicate Email/Username (409 Conflict)**
```bash
POST /user/add
{
  "email": "existing@example.com"
}

# Response:
{
  "status": 409,
  "message": "Data integrity violation: Email already exists",
  "path": "/user/add"
}
```

**2. Access Denied (403 Forbidden)**
```bash
DELETE /user/123/del
# Với user không có quyền ADMIN

# Response:
{
  "status": 403,
  "message": "Access denied: You do not have permission to access this resource",
  "path": "/user/123/del"
}
```

**3. Authentication Failed (401 Unauthorized)**
```bash
POST /auth/access-token
{
  "email": "user@example.com",
  "password": "wrong-password"
}

# Response:
{
  "status": 401,
  "message": "Authentication failed: Invalid username or password",
  "path": "/auth/access-token"
}
```

**4. Invalid JSON (400 Bad Request)**
```bash
POST /user/add
{
  "email": "invalid-json  # Missing closing brace
}

# Response:
{
  "status": 400,
  "message": "Invalid JSON format or malformed request body",
  "path": "/user/add"
}
```

---

### 4. 💾 DATABASE MIGRATIONS

#### Flyway Migrations

**Cấu trúc:**
```
src/main/resources/db/migration/
├── V1__initial_schema.sql      # Schema ban đầu
└── V2__seed_initial_data.sql   # Dữ liệu mẫu
```

#### Tạo Migration Mới

```bash
# 1. Tạo file mới theo format: V{version}__{description}.sql
# Ví dụ: V3__add_user_profile_table.sql

# 2. Viết SQL migration
cat > src/main/resources/db/migration/V3__add_user_profile_table.sql << 'EOF'
CREATE TABLE tbl_user_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    bio TEXT,
    avatar_url VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES tbl_user(id)
);
EOF

# 3. Restart application
# Flyway sẽ tự động chạy migration
```

#### Kiểm tra Migration Status

```bash
# Vào MySQL container
docker exec -it mysql-db mysql -uroot -p

USE backend-service;

# Xem migration history
SELECT * FROM flyway_schema_history;

# Output:
# +--------------+---------+---------------------+------+-------------+---------+
# | version      | success | installed_on        | type | script      | checksum|
# +--------------+---------+---------------------+------+-------------+---------+
# | 1            | 1       | 2025-10-21 10:00:00 | SQL  | V1__init... | 1234567 |
# | 2            | 1       | 2025-10-21 10:00:01 | SQL  | V2__seed... | 7654321 |
# +--------------+---------+---------------------+------+-------------+---------+
```

#### Rollback (Nếu Cần)

```bash
# Flyway không hỗ trợ auto-rollback
# Phải tạo migration mới để revert

# Ví dụ: V4__rollback_user_profile_table.sql
DROP TABLE IF EXISTS tbl_user_profile;
```

---

### 5. 🔐 SECURE ACTUATOR ENDPOINTS

#### Public Endpoints

```bash
# Health check - Không cần authentication
curl http://localhost:8081/actuator/health
# ✅ Response: {"status":"UP"}

# Info - Không cần authentication  
curl http://localhost:8081/actuator/info
# ✅ Response: Application info
```

#### Protected Endpoints (Cần ADMIN role)

```bash
# Metrics - Cần JWT token với ADMIN role
curl http://localhost:8081/actuator/metrics \
  -H "Authorization: Bearer YOUR_ADMIN_JWT_TOKEN"

# Nếu không có token hoặc không phải ADMIN:
# ❌ Response: 401 Unauthorized hoặc 403 Forbidden
```

---

### 6. 🐳 DOCKER IMPROVEMENTS

#### Health Checks

**Kiểm tra MySQL Health:**
```bash
docker inspect mysql-db | grep -A 20 Health

# Hoặc
docker exec mysql-db mysqladmin ping -h localhost -uroot -ppassword
```

**Kiểm tra Backend Health:**
```bash
docker inspect backend-service | grep -A 20 Health

# Hoặc
curl http://localhost:8081/actuator/health
```

#### Docker Image Size Comparison

```bash
# Build và so sánh size
docker-compose build

docker images | grep backend-service

# Before: ~400MB (openjdk:17)
# After:  ~200MB (eclipse-temurin:17-jre-alpine)
# Savings: 50% reduction! ✅
```

---

### 7. 🔧 ENVIRONMENT-BASED CONFIGURATION

#### Development Environment

```bash
# .env
SPRING_PROFILES_ACTIVE=dev
DB_URL=jdbc:mysql://localhost:3306/backend-service
DB_USERNAME=root
DB_PASSWORD=dev-password
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200
```

#### Production Environment

```bash
# .env
SPRING_PROFILES_ACTIVE=prod
PROD_DB_URL=jdbc:postgresql://prod-server:5432/backend-service
PROD_DB_USER=prod_user
PROD_DB_PASSWORD=strong-prod-password
CORS_ALLOWED_ORIGINS=https://yourdomain.com
JWT_ACCESS_KEY=production-access-key-base64
JWT_REFRESH_KEY=production-refresh-key-base64
```

#### Chạy với Profile Khác Nhau

```bash
# Development
export SPRING_PROFILES_ACTIVE=dev
mvn spring-boot:run

# Production
export SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run

# Docker
docker-compose up -e SPRING_PROFILES_ACTIVE=prod
```

---

## 🧪 TESTING

### Test Password Validation

```bash
# Test script
cat > test-password-validation.sh << 'EOF'
#!/bin/bash

echo "Testing password validation..."

# Invalid: too short
curl -X POST http://localhost:8081/user/add \
  -H "Content-Type: application/json" \
  -d '{"username":"test1","email":"test1@example.com","password":"Pass1!"}'
echo ""

# Invalid: no uppercase
curl -X POST http://localhost:8081/user/add \
  -H "Content-Type: application/json" \
  -d '{"username":"test2","email":"test2@example.com","password":"password123!"}'
echo ""

# Valid password
curl -X POST http://localhost:8081/user/add \
  -H "Content-Type: application/json" \
  -d '{"username":"test3","email":"test3@example.com","password":"ValidPass123!"}'
echo ""
EOF

chmod +x test-password-validation.sh
./test-password-validation.sh
```

### Test Exception Handling

```bash
# Test duplicate email
curl -X POST http://localhost:8081/user/add \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","email":"existing@example.com","password":"Pass123!"}'

# Test invalid JSON
curl -X POST http://localhost:8081/user/add \
  -H "Content-Type: application/json" \
  -d '{"username":"user2",'  # Invalid JSON

# Test access denied
curl -X DELETE http://localhost:8081/user/1/del
# Without admin token
```

### Test Database Migrations

```bash
# 1. Stop containers
docker-compose down -v  # Remove volumes

# 2. Start fresh
docker-compose up -d

# 3. Check migration logs
docker-compose logs backend-service | grep -i flyway

# Should see:
# Flyway: Successfully applied 2 migrations
```

---

## 📊 MONITORING

### Application Logs

```bash
# Real-time logs
docker-compose logs -f backend-service

# Filter by log level
docker-compose logs backend-service | grep "ERROR"
docker-compose logs backend-service | grep "WARN"

# Filter by endpoint
docker-compose logs backend-service | grep "/user/add"
```

### Database Performance

```bash
# Connect to MySQL
docker exec -it mysql-db mysql -uroot -p

# Check slow queries
SHOW FULL PROCESSLIST;

# Check index usage
EXPLAIN SELECT * FROM tbl_user WHERE email = 'test@example.com';

# Should use idx_email index
```

### Health Monitoring Script

```bash
cat > monitor.sh << 'EOF'
#!/bin/bash

while true; do
  echo "=== Health Check $(date) ==="
  
  # Backend health
  curl -s http://localhost:8081/actuator/health | jq .
  
  # MySQL health
  docker exec mysql-db mysqladmin ping -h localhost -uroot -p${DB_PASSWORD} 2>/dev/null
  
  echo ""
  sleep 30
done
EOF

chmod +x monitor.sh
./monitor.sh
```

---

## 🔍 TROUBLESHOOTING

### Problem: Application không start

```bash
# Check logs
docker-compose logs backend-service

# Common issues:
# 1. Missing environment variables
# 2. Database connection failed
# 3. Port already in use

# Solution:
# 1. Check .env file
# 2. Verify MySQL is running
# 3. Change port in .env or kill process using port 8081
```

### Problem: Flyway migration failed

```bash
# Check migration history
docker exec -it mysql-db mysql -uroot -p
USE backend-service;
SELECT * FROM flyway_schema_history;

# If migration failed, you may need to:
# 1. Fix the SQL syntax error
# 2. Delete failed migration entry
# 3. Restart application

DELETE FROM flyway_schema_history WHERE version = 'X' AND success = 0;
```

### Problem: Password validation không hoạt động

```bash
# Verify annotation is present
grep -r "@ValidPassword" src/

# Check logs for validation errors
docker-compose logs backend-service | grep "PasswordValidator"

# Make sure pom.xml includes jakarta.validation
```

---

## 🎯 BEST PRACTICES

### 1. Quản Lý Environment Variables

```bash
# ĐÚNG ✅
# .env (không commit lên Git)
DB_PASSWORD=your-secure-password
JWT_ACCESS_KEY=your-secret-key

# SAI ❌
# application.yml
spring:
  datasource:
    password: 123456  # Hardcoded!
```

### 2. Password Requirements

```bash
# ĐÚNG ✅
MySecureP@ssw0rd    # 8+ chars, mixed case, number, special
Admin#2025xyz       # Strong password

# SAI ❌
password            # Quá yếu
12345678            # Chỉ có số
```

### 3. CORS Configuration

```bash
# Development ✅
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:4200

# Production ✅
CORS_ALLOWED_ORIGINS=https://yourdomain.com

# SAI ❌
CORS_ALLOWED_ORIGINS=*  # Cho phép tất cả origins!
```

---

## 📞 HỖ TRỢ

**Tài liệu liên quan:**
- `README.md` - Hướng dẫn cơ bản
- `IMPROVEMENTS.md` - Danh sách cải tiến
- `IMPROVEMENTS_COMPLETED.md` - Chi tiết cải tiến (English)
- `CAI_TIEN_HOAN_THANH.md` - Chi tiết cải tiến (Tiếng Việt)

**Liên hệ:**
- Tạo issue trên repository
- Liên hệ development team

---

**Cập nhật lần cuối:** 21/10/2025  
**Phiên bản:** 1.0

