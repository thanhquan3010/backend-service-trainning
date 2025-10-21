# Backend Service

A robust Spring Boot REST API service for user management with JWT authentication, email verification, and role-based access control.

## 🚀 Features

- **Authentication & Authorization**: JWT-based authentication with access/refresh tokens
- **User Management**: Complete CRUD operations for user accounts
- **Email Verification**: SendGrid integration for email verification
- **Role-Based Access Control**: Admin and user role management
- **Address Management**: User address handling with multiple address types
- **API Documentation**: Swagger/OpenAPI 3.0 documentation
- **Database Support**: MySQL (production), H2 (testing)
- **Docker Support**: Containerized deployment with Docker Compose
- **Security**: Spring Security with CORS configuration
- **Validation**: Comprehensive input validation with custom error handling

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot 3.5.6**
- **Spring Security 6**
- **Spring Data JPA**
- **MySQL 8.0** / **H2 Database**
- **JWT (JSON Web Tokens)**
- **SendGrid** (Email service)
- **MapStruct** (Object mapping)
- **Lombok** (Code generation)
- **Swagger/OpenAPI 3.0**
- **Docker & Docker Compose**
- **Maven** (Build tool)

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- MySQL 8.0+ (for production)
- Docker & Docker Compose (optional)

## 🚀 Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd backend-service
```

### 2. Environment Setup

Copy the environment example file and configure your variables:

```bash
cp env.example .env
```

Edit `.env` file with your configuration:

```env
# JWT Configuration
JWT_ACCESS_KEY=your-base64-encoded-access-key-here
JWT_REFRESH_KEY=your-base64-encoded-refresh-key-here

# Database Configuration (Production)
PROD_DB_URL=jdbc:mysql://localhost:3306/backend-service-prod?useSSL=false&serverTimezone=UTC
PROD_DB_USER=your-production-username
PROD_DB_PASSWORD=your-production-password

# SendGrid Configuration
SENDGRID_API_KEY=your-sendgrid-api-key-here
SENDGRID_FROM_EMAIL=your-email@example.com
SENDGRID_TEMPLATE_ID=your-template-id-here

# Application Configuration
SERVER_PORT=8081
SPRING_PROFILES_ACTIVE=dev
```

### 3. Database Setup

#### Option A: Using Docker Compose (Recommended)

```bash
docker-compose up -d mysql
```

#### Option B: Manual MySQL Setup

1. Create a MySQL database:
```sql
CREATE DATABASE `backend-service`;
```

2. Update `application-dev.yml` with your database credentials:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/backend-service?useSSL=false&serverTimezone=UTC
    username: your-username
    password: your-password
```

### 4. Run the Application

#### Development Mode

```bash
mvn spring-boot:run
```

#### Using Maven Profiles

```bash
# Development
mvn spring-boot:run -Pdev

# Testing
mvn spring-boot:run -Ptest

# Production
mvn spring-boot:run -Pprod
```

#### Using Docker

```bash
# Build and run with Docker Compose
docker-compose up --build

# Or build Docker image manually
docker build -t backend-service .
docker run -p 8081:8081 backend-service
```

## 📚 API Documentation

Once the application is running, access the API documentation:

- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8081/v3/api-docs

## 🔐 Authentication

### JWT Token Flow

1. **Login**: `POST /auth/access-token`
   ```json
   {
     "email": "user@example.com",
     "password": "password123"
   }
   ```

2. **Response**:
   ```json
   {
     "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
   }
   ```

3. **Refresh Token**: `POST /auth/refresh-token`
   ```json
   "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
   ```

### Authorization

Include the JWT token in the Authorization header:

```bash
Authorization: Bearer <your-access-token>
```

## 📖 API Endpoints

### Authentication
- `POST /auth/access-token` - Login and get tokens
- `POST /auth/refresh-token` - Refresh access token

### User Management
- `GET /user/list` - Get all users (with pagination)
- `GET /user/{userId}` - Get user by ID
- `POST /user/add` - Create new user
- `PUT /user/update` - Update user
- `PATCH /user/change-pwd` - Change password
- `DELETE /user/{userId}/del` - Delete user (Admin only)
- `GET /user/confirm-email` - Confirm email verification

### Email Service
- `POST /email/send` - Send email (internal use)

## 🗄️ Database Schema

### Core Entities

#### UserEntity
- `id` (Primary Key)
- `firstName`, `lastName`
- `email` (Unique)
- `username` (Unique)
- `password` (Encrypted)
- `phone`
- `gender`, `birthday`
- `type` (UserType: ADMIN, USER)
- `status` (UserStatus: ACTIVE, INACTIVE, NONE)
- `verificationCode`
- `createdAt`, `updatedAt`

#### AddressEntity
- `id` (Primary Key)
- `userId` (Foreign Key)
- `apartmentNumber`, `floor`, `building`
- `streetNumber`, `street`
- `city`, `country`
- `addressType`

#### Role & Permission System
- `Role` - User roles (ADMIN, USER)
- `Permission` - System permissions
- `UserHasRole` - User-role mapping
- `RoleHasPermission` - Role-permission mapping

## 🧪 Testing

### Run Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserControllerTest

# Run with test profile
mvn test -Ptest
```

### Test Configuration

The application uses H2 in-memory database for testing:
- **H2 Console**: http://localhost:8081/h2-console (when running with test profile)
- **Database URL**: `jdbc:h2:mem:testdb`
- **Username**: `sa`
- **Password**: (empty)

## 🐳 Docker Deployment

### Docker Compose

The `docker-compose.yml` includes:
- **MySQL 8.0** database
- **Backend Service** application

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f backend-service

# Stop services
docker-compose down
```

### Environment Variables for Docker

Set these environment variables for production:

```bash
export JWT_ACCESS_KEY="your-production-access-key"
export JWT_REFRESH_KEY="your-production-refresh-key"
export PROD_DB_URL="jdbc:mysql://mysql:3306/backend-service"
export PROD_DB_USER="root"
export PROD_DB_PASSWORD="password"
```

## 🔧 Configuration

### Application Profiles

#### Development (`dev`)
- MySQL database
- SQL logging enabled
- Swagger UI enabled
- Debug logging

#### Testing (`test`)
- H2 in-memory database
- SQL logging disabled
- Swagger UI enabled

#### Production (`prod`)
- PostgreSQL database (configurable)
- Swagger UI disabled
- Minimal logging

### JWT Configuration

```yaml
jwt:
  secret:
    access-key: ${JWT_ACCESS_KEY:default-key}
    refresh-key: ${JWT_REFRESH_KEY:default-key}
```

### SendGrid Configuration

```yaml
spring:
  sendGrid:
    apiKey: ${SENDGRID_API_KEY:your-api-key}
    fromEmail: ${SENDGRID_FROM_EMAIL:your-email@example.com}
    templateId: ${SENDGRID_TEMPLATE_ID:your-template-id}
    verificationLink: ${VERIFICATION_LINK:http://localhost:8080/user/confirm-email}
```

## 🚨 Security Considerations

### JWT Security
- Access tokens expire in 15 minutes
- Refresh tokens expire in 7 days
- Tokens are signed with HMAC-SHA256
- Different keys for access and refresh tokens

### Password Security
- Passwords are encrypted using BCrypt
- Minimum 8 characters required
- Must contain uppercase, lowercase, number, and special character

### CORS Configuration
- Configured for development (allows all origins)
- Should be restricted in production
- Supports credentials

## 📊 Monitoring & Health Checks

### Actuator Endpoints

Access health and monitoring endpoints:

- **Health Check**: http://localhost:8081/actuator/health
- **Info**: http://localhost:8081/actuator/info
- **Metrics**: http://localhost:8081/actuator/metrics

### Logging

Logging is configured with different levels:
- **Root**: INFO
- **Application**: DEBUG
- **Web**: INFO

## 🐛 Troubleshooting

### Common Issues

#### 1. Database Connection Failed
```bash
# Check if MySQL is running
docker-compose ps mysql

# Check database credentials in application-dev.yml
```

#### 2. JWT Token Invalid
```bash
# Verify JWT keys are properly set
echo $JWT_ACCESS_KEY
echo $JWT_REFRESH_KEY
```

#### 3. Email Service Not Working
```bash
# Check SendGrid configuration
# Verify API key and template ID
```

#### 4. Port Already in Use
```bash
# Change port in application.yml
server:
  port: 8082
```

### Logs

```bash
# View application logs
tail -f logs/application.log

# Docker logs
docker-compose logs -f backend-service
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow Java coding conventions
- Write unit tests for new features
- Update documentation for API changes
- Use meaningful commit messages

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Team

- **Backend Development**: Spring Boot, Security, JWT
- **Database Design**: MySQL, JPA, Hibernate
- **DevOps**: Docker, Docker Compose
- **Documentation**: OpenAPI, Swagger

## 📞 Support

For support and questions:
- Create an issue in the repository
- Contact the development team
- Check the documentation and troubleshooting section

---

**Happy Coding! 🚀**
