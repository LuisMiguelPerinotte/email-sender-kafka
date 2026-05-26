## 📨 Email Sender Kafka

Asynchronous email sending API built with Spring Boot and Apache Kafka.

The main goal of this project is to demonstrate asynchronous communication using Kafka, email request persistence, and event-driven architecture concepts.


> **Project Status:** In development 🚧 — contributions, issues and pull requests are welcome.


&nbsp;&nbsp;&nbsp;
## 🏗️ Architecture

```text
Client
   ↓
email-api
   ↓
PostgreSQL
   ↓
Kafka Topic: email.send.requested
   ↓
email-worker
   ↓
Mailpit SMTP Server
```


&nbsp;&nbsp;&nbsp;
## ⚙️ Email Processing Flow

1. Client sends a request to `POST /emails`
2. API stores the email request as `PENDING`
3. API publishes an event to Kafka
4. Worker consumes the event
5. Worker updates the status to `PROCESSING`
6. Worker sends the email using Mailpit
7. Worker updates the status to `SENT` or `FAILED`


&nbsp;&nbsp;&nbsp;
## 🛠️ Tech Stack

* Java 21
* Spring Boot 3
* Apache Kafka
* Spring Kafka
* PostgreSQL
* Flyway
* Docker
* Lombok
* Mailpit
* Kafka UI


&nbsp;&nbsp;&nbsp;
## ✨ Current Features

- Create asynchronous email requests
- Persist email requests into PostgreSQL
- Publish events to Kafka
- Consume Kafka events using a worker service
- Send emails through Mailpit SMTP server
- Track email processing status
- Handle Kafka publishing failures


&nbsp;&nbsp;&nbsp;
## 📌 Email Request Status

| Status         | Description                                      |
| -------------- | ------------------------------------------------ |
| PENDING        | Email request created and waiting for processing |
| PUBLISH_FAILED | Failed to publish event to Kafka                 |
| PROCESSING     | Email is being processed by the worker           |
| SENT           | Email successfully sent                          |
| FAILED         | Error while sending email                        |


&nbsp;&nbsp;&nbsp;
## 📂 Project Structure

```text
email-sender-kafka/
├── email-api/
├── email-worker/
├── docker-compose.yml
└── README.md
```


&nbsp;&nbsp;&nbsp;
## 🐳 Local Infrastructure

The project uses Docker Compose to run:

- PostgreSQL
- Apache Kafka
- Kafka UI
- Mailpit


&nbsp;&nbsp;&nbsp;
## 🚀 Running the Project

### 1. Clone the repository

```bash
git clone https://github.com/your-username/email-sender-kafka.git
```

### 2. Configure environment variables

Create a `.env` file at the project root:

```env
LOCAL_POSTGRES_DB=email_sender
LOCAL_POSTGRES_DB_USER=postgres
LOCAL_POSTGRES_DB_PASSWORD=postgres
```

### 3. Start containers

```bash
docker compose up -d
```

### 4. Run the application

Start the `email-api` module.


&nbsp;&nbsp;&nbsp;
## 🌐 API Endpoints

### Create email request

```http
POST /emails
```

**Request**

```json
{
  "recipient": "user@email.com",
  "subject": "Hello",
  "body": "Testing Kafka"
}
```

**Response**

```json
{
  "emailRequestId": "uuid",
  "status": "PENDING"
}
```


&nbsp;&nbsp;&nbsp;
### Get email request by ID

```http
GET /emails/{id}
```

**Response**

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


&nbsp;&nbsp;&nbsp;
## 📡 Kafka

### Topic

```text
email.send.requested
```

### Published Event

```json
{
  "emailRequestId": "uuid"
}
```


&nbsp;&nbsp;&nbsp;
## 📬 Mailpit

Mailpit is used as a local SMTP server for development and testing.

### Mailpit UI

```text
http://localhost:8025
```


&nbsp;&nbsp;&nbsp;
## 📈 Next Steps

- Retry mechanism
- Dead Letter Queue (DLQ)
- Automated tests
- Metrics and observability
- Dockerization improvements
