# Construction Workforce Management System

## Overview

A Spring Boot based Workforce Management System for construction sites that manages workers, sites, attendance tracking, overtime calculations, and overtime settlements.

The system is designed to handle real-world workforce operations where site supervisors manage worker attendance, payroll teams process overtime settlements, and active workforce data needs to be retrieved efficiently.

---

## Features

### Worker Management

- Create workers
- Update worker details
- Activate / deactivate workers
- Track designation and wage rates

### Site Management

- Create construction sites
- Update site details
- Activate / deactivate sites

### Attendance Management

- Clock-in workers at sites
- Prevent duplicate active clock-ins
- Clock-out workers
- Calculate total hours worked automatically
- Calculate overtime hours automatically
- Flag shifts longer than 16 hours
- Attendance history with pagination

### Active Workers Cache

- Stores currently active workers in Redis
- Supports fast retrieval without hitting PostgreSQL
- Gracefully degrades when Redis is unavailable

### Overtime Management

- Automatically creates overtime entries during clock-out
- Supports overtime summary reporting
- Enforces monthly overtime cap of 60 hours
- Supports overtime settlement workflow
- Settlement is atomic using transactions

---

## Technology Stack

| Technology            | Purpose               |
| --------------------- | --------------------- |
| Java 17               | Backend Development   |
| Spring Boot 2.7       | Application Framework |
| Spring Data JPA       | ORM Layer             |
| Hibernate             | Persistence Provider  |
| PostgreSQL (Supabase) | Primary Database      |
| Redis                 | Caching Layer         |
| Maven                 | Dependency Management |
| Lombok                | Boilerplate Reduction |

---

## System Architecture

```text
Client
   ↓
Controllers
   ↓
Services
   ↓
Repositories
   ↓
PostgreSQL (Supabase)

Redis
 ↑
Active Worker Cache
```

---

## Database Design

### Worker

Stores worker information including:

- Name
- Phone Number
- Designation
- Daily Wage Rate
- Active Status

### Site

Stores construction site information including:

- Site Name
- Location
- Active Status

### Attendance Log

Stores:

- Clock In Time
- Clock Out Time
- Total Hours Worked
- Overtime Hours
- Flagged Status

### Overtime Entry

Stores:

- Overtime Date
- Overtime Hours
- Overtime Amount
- Settlement Status

---

## API Endpoints

### Worker APIs

```http
POST /api/workers
GET /api/workers
GET /api/workers/{id}
PUT /api/workers/{id}
DELETE /api/workers/{id}
```

### Site APIs

```http
POST /api/sites
GET /api/sites
GET /api/sites/{id}
PUT /api/sites/{id}
DELETE /api/sites/{id}
```

### Attendance APIs

```http
POST /api/attendance/clock-in
POST /api/attendance/clock-out
GET /api/attendance/active
GET /api/attendance/log
```

### Overtime APIs

```http
GET /api/overtime/summary/{workerId}
POST /api/overtime/settle/{workerId}
```

---

## Redis Strategy

Redis is used only for active worker tracking.

### Why Redis?

The active workers endpoint is frequently accessed by supervisors and dashboards.

Instead of querying the database repeatedly:

```text
Clock In
↓
Store Active Worker in Redis

Clock Out
↓
Remove Active Worker from Redis
```

This provides faster lookups and reduces database load.

### Redis Failure Handling

If Redis becomes unavailable:

- Application continues to start
- Attendance operations continue working
- PostgreSQL remains the source of truth
- Cache operations fail gracefully

---

## Overtime Rules

### Standard Shift

```text
8 Hours
```

### Overtime Rules

```text
First 2 Overtime Hours  → 1.5x Rate
Remaining Hours         → 2.0x Rate
```

### Monthly Cap

```text
Maximum 60 Overtime Hours Per Month
```

### Long Shift Flagging

```text
Shifts > 16 Hours
```

are automatically flagged for review.

---

## Ticket Blitz Fixes

### LF-201

Implemented centralized CORS configuration with environment-based allowed origins.

### LF-202

Implemented Redis degradation strategy allowing the application to function when Redis is unavailable.

### LF-203

Added pagination and eliminated N+1 query issues using EntityGraph.

### LF-204

Implemented transactional overtime settlement and after-commit event handling for notifications.

### LF-205

Configured HikariCP for Supabase and connection pool tuning.

---

## Setup Instructions

### PostgreSQL (Supabase)

Configure environment variables:

```bash
export DB_URL=...
export DB_USERNAME=...
export DB_PASSWORD=...
```

### Redis

Install:

```bash
brew install redis
```

Start:

```bash
redis-server
```

### Run Application

```bash
mvn spring-boot:run
```

---

## Postman Usage

### Base URL

```text
http://localhost:8080
```

### Worker APIs

| Method | Endpoint            |
| ------ | ------------------- |
| POST   | `/api/workers`      |
| GET    | `/api/workers`      |
| GET    | `/api/workers/{id}` |
| PUT    | `/api/workers/{id}` |
| DELETE | `/api/workers/{id}` |

#### Create Worker Request Body

```json
{
  "name": "ALex Bell",
  "phone": "9999999999",
  "designation": "MASON",
  "dailyWageRate": 800
}
```

---

### Site APIs

| Method | Endpoint          |
| ------ | ----------------- |
| POST   | `/api/sites`      |
| GET    | `/api/sites`      |
| GET    | `/api/sites/{id}` |
| PUT    | `/api/sites/{id}` |
| DELETE | `/api/sites/{id}` |

#### Create Site Request Body

```json
{
  "siteName": "Whitefield Project",
  "location": "Bangalore"
}
```

---

### Attendance APIs

| Method | Endpoint                    |
| ------ | --------------------------- |
| POST   | `/api/attendance/clock-in`  |
| POST   | `/api/attendance/clock-out` |
| GET    | `/api/attendance/active`    |
| GET    | `/api/attendance/log`       |

#### Clock In Request

```json
{
  "workerId": 1,
  "siteId": 1
}
```

#### Clock Out Request

```json
{
  "workerId": 1
}
```

#### Attendance Log Example

```http
GET /api/attendance/log?workerId=1&from=2026-06-01&to=2026-06-30&page=0&size=20
```

---

### Overtime APIs

| Method | Endpoint                           |
| ------ | ---------------------------------- |
| GET    | `/api/overtime/summary/{workerId}` |
| POST   | `/api/overtime/settle/{workerId}`  |

#### Overtime Summary Example

```http
GET /api/overtime/summary/1?month=2026-06
```

#### Overtime Settlement Example

```http
POST /api/overtime/settle/1?month=2026-05
```

---

### Recommended Testing Order

1. Create Worker
2. Create Site
3. Clock In Worker
4. Clock Out Worker
5. Check Active Workers
6. Check Attendance History
7. Check Overtime Summary
8. Settle Overtime

---

## AI Usage

AI tools were used for:

- Design validation
- Spring Boot implementation guidance
- Debugging support
- Architecture review

All code was reviewed, integrated, tested, and adapted manually.

---

## Design Decisions

### Why DTOs?

To avoid exposing JPA entities directly and prevent lazy-loading serialization issues.

### Why Redis?

To optimize active worker lookups while keeping PostgreSQL as the system of record.

### Why Transactional Settlement?

To ensure overtime settlement remains atomic and prevents partial payroll updates.

### Why Event-Based Notifications?

Notifications are triggered only after successful transaction commits, preventing false settlement messages.

---

## Future Improvements

- JWT Authentication
- Role Based Access Control
- SMS Gateway Integration
- Audit Logging
- Monitoring & Metrics
- Docker Deployment
- CI/CD Pipeline
