# rilybricoule-back

A minimal Spring Boot project implementing JWT-based authentication and authorization with three roles: CLIENT, PROVIDER, and ADMIN.

## Setup

1. Ensure PostgreSQL is running and create a database named `jwt_auth_db`.
2. Update `application.properties` with your PostgreSQL credentials if needed.
3. Build and run the application:
   ```
   mvn clean install
   mvn spring-boot:run
   ```

## API Endpoints

### Authentication

#### Register
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "password",
    "role": "ADMIN"
  }'
```

#### Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@example.com",
    "password": "password"
  }'
```

Use the returned token in the Authorization header for protected endpoints: `Authorization: Bearer <token>`

### Protected Endpoints

#### Admin Dashboard
```bash
curl -X GET http://localhost:8080/admin/dashboard \
  -H "Authorization: Bearer <token>"
```

#### Provider Dashboard
```bash
curl -X GET http://localhost:8080/provider/dashboard \
  -H "Authorization: Bearer <token>"
```

#### Client Dashboard
```bash
curl -X GET http://localhost:8080/client/dashboard \
  -H "Authorization: Bearer <token>"
```

## Project Structure

```
src/main/java/com/example/jwtauth/
├── auth/
│   ├── AuthController.java
│   ├── AuthService.java
│   ├── GlobalExceptionHandler.java
│   └── dto/
│       ├── AuthResponse.java
│       ├── LoginRequest.java
│       └── RegisterRequest.java
├── controller/
│   ├── AdminController.java
│   ├── ClientController.java
│   └── ProviderController.java
├── security/
│   ├── CustomUserDetailsService.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtUtil.java
│   └── SecurityConfig.java
└── user/
    ├── Role.java
    ├── User.java
    ├── UserRepository.java
    └── UserService.java
```

## Database Schema

The `users` table is created automatically with the following columns:
- id (BIGINT, PRIMARY KEY)
- email (VARCHAR, UNIQUE)
- password (VARCHAR)
- role (VARCHAR)
