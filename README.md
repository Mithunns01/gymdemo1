# Gym Membership & Fitness Management System

A comprehensive REST API-based Gym Management System built with Spring Boot 3.2, Spring Security with JWT authentication, and MySQL database.

## Features

### Authentication & Authorization
- JWT-based authentication with token generation and validation
- Role-based access control (Admin, Trainer, Member)
- Secure password encryption using BCrypt

### Member Module
- Register new members
- Update member profiles
- View membership details and history
- View assigned trainers
- View workout plans
- Mark daily attendance (once per day)
- View attendance history
- View payment history
- BMI Calculator

### Trainer Module
- View assigned members
- Create and update workout plans
- Record member fitness progress
- View attendance reports
- Add fitness notes

### Admin Module
- Add/Edit/Delete Trainers
- Add/Edit/Delete Members (soft delete)
- Manage membership plans (CRUD)
- Manage membership renewals
- View payment reports
- View attendance reports
- Dashboard statistics with charts data
- Search members by name, phone, or membership ID (username)

### Bonus Features
- BMI Calculator
- Membership expiry notifications (via API)
- Dashboard statistics with monthly revenue & attendance chart data
- Export attendance report to Excel

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security** (JWT Authentication)
- **Spring Data JPA / Hibernate**
- **MySQL 8**
- **Maven**
- **JUnit 5** with MockMvc
- **Swagger / OpenAPI 3** (springdoc-openapi)
- **Apache POI** (Excel export)
- **JaCoCo** (Code coverage)

## Prerequisites

- JDK 17 or later
- Maven 3.8+
- MySQL 8.0+
- Git

## Setup & Installation

### 1. Clone the Repository
```bash
git clone <repository-url>
cd gymdemo
```

### 2. Configure MySQL Database

Create a MySQL database:

```sql
CREATE DATABASE gymdemo;
```

Or run the schema script:

```bash
mysql -u root -p < src/main/resources/sql/schema.sql
```

### 3. Configure Application Properties

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/gymdemo?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=your_password
```

### 4. Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080`.

### 5. Default Data

On first run, the application automatically seeds the database with:

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | ADMIN |
| trainer1 | trainer123 | TRAINER |
| trainer2 | trainer123 | TRAINER |
| member1 | member123 | MEMBER |
| member2 | member123 | MEMBER |

## API Documentation

### Swagger UI
Once the application is running, access Swagger UI at:
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON
```
http://localhost:8080/api-docs
```

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/auth/login` | User login (Admin/Trainer/Member) |
| POST | `/auth/register` | Register new member |

### Members
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/members` | Get all members |
| GET | `/members/{id}` | Get member by ID |
| PUT | `/members/{id}` | Update member |
| DELETE | `/members/{id}` | Deactivate member |
| GET | `/members/{id}/trainer` | Get assigned trainer |
| GET | `/members/{id}/membership` | Get active membership |
| GET | `/members/{id}/memberships` | Get membership history |
| GET | `/members/{id}/workouts` | Get workout plans |
| GET | `/members/{id}/attendance` | Get attendance history |
| GET | `/members/{id}/payments` | Get payment history |
| GET | `/members/search?keyword=` | Search members |
| POST | `/members/bmi` | Calculate BMI |
| GET | `/members/by-trainer/{trainerId}` | Get members by trainer |

### Trainers
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/trainers` | Get all trainers |
| GET | `/trainers/{id}` | Get trainer by ID |
| POST | `/trainers` | Create trainer (Admin) |
| PUT | `/trainers/{id}` | Update trainer |
| DELETE | `/trainers/{id}` | Delete trainer |
| GET | `/trainers/{id}/members` | Get assigned members |
| POST | `/trainers/workout` | Create workout plan |
| PUT | `/trainers/workout/{id}` | Update workout plan |
| GET | `/trainers/{id}/workouts` | Get trainer workout plans |
| GET | `/trainers/member-count` | Trainer-wise member count |

### Admin
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/plans` | Get all plans |
| POST | `/admin/plans` | Create plan |
| PUT | `/admin/plans/{id}` | Update plan |
| POST | `/admin/renew` | Renew membership |
| GET | `/admin/members/expiring?days=30` | Get expiring memberships |
| GET | `/admin/payments` | Get all payments |
| POST | `/admin/payments` | Record payment |
| GET | `/admin/payments/revenue?start=&end=` | Get revenue |
| GET | `/admin/payments/report?year=` | Monthly revenue report |
| POST | `/admin/attendance` | Mark attendance |
| GET | `/admin/attendance/daily?date=` | Daily attendance report |
| POST | `/admin/members/{id}/progress` | Update member progress |
| GET | `/admin/dashboard` | Dashboard statistics |

### Attendance
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/attendance` | Mark attendance (Member) |
| GET | `/attendance/{memberId}` | Get attendance history |

### Payments
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/payments` | Get all payments |
| POST | `/payments` | Record payment |
| GET | `/payments/report?year=` | Monthly revenue chart data |

### Workout Plans
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/workout` | Create workout plan |
| PUT | `/workout/{id}` | Update workout plan |

### Dashboard
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/dashboard` | Get dashboard statistics |

### Export
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/export/attendance?date=` | Export attendance report (Excel) |

## Business Rules

1. **Attendance**: Members cannot mark attendance more than once per day (enforced by unique constraint)
2. **Expired Memberships**: Expired memberships cannot access attendance marking
3. **Trainer Access**: Trainers can only manage assigned members
4. **Renewal**: Membership renewal automatically extends the expiry date
5. **Payment Records**: Payment records cannot be deleted (no delete endpoint)
6. **Attendance**: Only recorded for active memberships
7. **Members**: Soft-deleted (active flag set to false, not actually removed)

## Testing

```bash
mvn test
```

The tests use H2 in-memory database and include:
- Unit tests for services
- Integration tests for controllers
- REST API testing with MockMvc

To generate code coverage report:
```bash
mvn clean test jacoco:report
```
Report will be at `target/site/jacoco/index.html`

## API Authentication

### Login
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

### Use Token
```bash
curl -X GET http://localhost:8080/dashboard \
  -H "Authorization: Bearer <your-jwt-token>"
```

## Database Schema

### Tables
1. **users** - User accounts with roles
2. **membership_plans** - Available membership plans
3. **trainers** - Trainer profiles
4. **members** - Member profiles
5. **member_memberships** - Member-plan assignments with dates
6. **workout_plans** - Workout plans assigned to members
7. **attendance** - Daily attendance records
8. **payments** - Payment transactions

### ER Diagram

See `ER_Diagram.md` for the entity-relationship diagram description. You can generate the visual diagram using tools like MySQL Workbench or draw.io based on the schema.

## Deployment

### Build JAR
```bash
mvn clean package -DskipTests
```

### Run JAR
```bash
java -jar target/gymdemo-0.0.1-SNAPSHOT.jar
```

### Docker (Optional)
```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim
COPY target/gymdemo-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## Contributing

Please read CONTRIBUTING.md for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the MIT License.

