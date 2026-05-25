# Email Sender Kafka

Asynchronous email sending API built with Spring Boot and Apache Kafka.

The main goal of this project is to demonstrate asynchronous communication using Kafka, email request persistence, and event-driven architecture concepts.

---

## Project Status

### 🚧 In Progress

This project is currently under active development.
Core asynchronous email request processing is already implemented, while worker processing, retry mechanisms, DLQ handling and automated tests are still in progress.

---

# Architecture

```text
Client
   ↓
email-api
   ↓
PostgreSQL
   ↓
Kafka Topic: email.send.requested
   ↓
email-worker (WIP)
```

---

# Tech Stack

* Java 21
* Spring Boot 3
* Apache Kafka
* Spring Kafka
* PostgreSQL
* Flyway
* Docker
* Lombok
* Mailpit (WIP)
* Kafka UI

---

# Current Features

* Create email sending requests
* Persist requests into PostgreSQL
* Publish events to Kafka
* Retrieve email request status
* Handle Kafka publishing failures

---

# Email Request Status

| Status         | Description                                      |
| -------------- | ------------------------------------------------ |
| PENDING        | Email request created and waiting for processing |
| PUBLISH_FAILED | Failed to publish event to Kafka                 |

---

# Project Structure

```text
email-sender-kafka/
├── email-api/
├── email-worker/ (WIP)
├── docker-compose.yml
└── README.md
```

---

# Local Infrastructure

The project uses Docker Compose to run:

* PostgreSQL
* Apache Kafka
* Kafka UI

---

# Running the Project

## 1. Clone the repository

```bash
git clone https://github.com/your-username/email-sender-kafka.git
```

---

## 2. Configure environment variables

Create a `.env` file at the project root:

```env
LOCAL_POSTGRES_DB=email_sender
LOCAL_POSTGRES_DB_USER=postgres
LOCAL_POSTGRES_DB_PASSWORD=postgres
```

---

## 3. Start containers

```bash
docker compose up -d
```

---

## 4. Run the application

Start the `email-api` module.

---

# API Endpoints

## Create email request

```http
POST /emails
```

### Request

```json
{
  "recipient": "user@email.com",
  "subject": "Hello",
  "body": "Testing Kafka"
}
```

### Response

```json
{
  "emailRequestId": "uuid",
  "status": "PENDING"
}
```

---

## Get email request by ID

```http
GET /emails/{id}
```

### Response

```json
{
  "emailRequestId": "uuid",
  "recipient": "user@email.com",
  "subject": "Hello",
  "status": "PENDING",
  "attempts": 0,
  "createdAt": "2026-05-25T12:00:00"
}
```

---

# Kafka

## Topic

```text
email.send.requested
```

## Published Event

```json
{
  "emailRequestId": "uuid"
}
```

---

# Next Steps

* Implement `email-worker`
* Asynchronous email processing
* Mailpit integration
* Retry mechanism
* Dead Letter Queue (DLQ)
* Automated tests
* Metrics and observability