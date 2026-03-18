# AI Integrated Claims Processing System

I built this project to learn and demonstrate how AI can be integrated into a real backend system. It's an insurance claims processing API where every claim gets automatically analysed by an AI model for fraud risk before it's processed.

---

## The idea

When someone submits an insurance claim, there's always a risk of fraud. Instead of manually reviewing every claim, I integrated Groq's LLM to analyse the claim details and return a fraud risk score — LOW, MEDIUM, or HIGH. If the risk is HIGH, the system blocks approval and requires underwriter review.

The notification part is handled separately via Kafka. Once a claim is submitted or its status changes, an event is published to a Kafka topic and the notification service picks it up and generates the appropriate message.

---

## Services

- ai-claims-service — the core service. Handles claim submission, calls Groq AI, caches results in Redis, saves to DB, and publishes Kafka events.
- notification-service — listens to Kafka and generates notifications based on claim status changes.
- user-management — reused from my Loan Eligibility System. Handles user registration, login and JWT tokens.

---

## What I have implemented

- Groq AI integration for fraud detection on every claim
- Circuit breaker on the AI call — if Groq is down, claim still gets saved with MEDIUM risk and flagged for manual review
- Kafka for async event-driven notifications — no direct coupling between claims and notification service
- Redis caching for AI assessment results
- Rate limiting — 5 claim submissions per hour per user
- Idempotency keys to prevent duplicate submissions
- OpenFeign to call user-management for user verification
- JWT authentication
- Docker Compose to run everything together

---

## Tech stack

- Java 17, Spring Boot 3
- MySQL, JPA/Hibernate
- Redis
- Apache Kafka
- Groq API (llama-3.3-70b-versatile)
- Resilience4j for circuit breaker
- Bucket4j for rate limiting
- OpenFeign
- Docker, Docker Compose

---

## Running locally

Make sure you have Docker Desktop installed and the user-management image built from the Loan Eligibility System.

Copy the example config and fill in your values:

    cp docker-compose.example.yml docker-compose.yml

Start everything:

    docker-compose up --build

Services will be available at:
- User Management: http://localhost:8081
- Claims Service: http://localhost:8083
- Notification Service: http://localhost:8084

---

## How to use

First register and login via user-management to get a JWT token, then use it to submit claims.

Login:

    POST http://localhost:8081/api/auth/login
    {
        "usernameOrEmail": "john",
        "password": "pass123"
    }

Submit a claim:

    POST http://localhost:8083/api/claims/submit
    Authorization: Bearer your_token_here
    {
        "claimType": "VEHICLE",
        "idempotencyKey": "unique-key-001",
        "claimDetail": {
            "damageCode": "VH-001",
            "damagedItem": "Honda City",
            "incidentDate": "2026-03-15",
            "incidentLocation": "Hyderabad",
            "incidentDescription": "Car damaged in road accident on highway",
            "causeType": "ARTIFICIAL",
            "estimatedAmount": 150000
        }
    }

Other endpoints:

    GET  /api/claims/my-claims           — view all my claims
    GET  /api/claims/{id}                — view a specific claim
    GET  /api/claims/{id}/history        — view status history
    PUT  /api/claims/{id}/review         — admin: mark under review
    PUT  /api/claims/{id}/approve        — admin: approve (blocked if HIGH fraud risk)
    PUT  /api/claims/{id}/reject?reason  — admin: reject with reason
    PUT  /api/claims/{id}/settle         — admin: settle the claim

---

## Claim lifecycle

    SUBMITTED → UNDER_REVIEW → APPROVED → SETTLED
                        ↓
                    REJECTED

---

## AI fraud assessment

Every submitted claim goes through Groq AI analysis.

- LOW risk — claim can be approved normally
- MEDIUM risk — claim can be approved normally
- HIGH risk — approval blocked, underwriter authorization required

If Groq AI is unavailable, the circuit breaker kicks in and the claim is saved with MEDIUM risk and a note saying manual review is required. The claim submission never fails because AI is down.

---

## Kafka events

The claims service publishes an event to the claim-events topic on every status change. The notification service consumes these and generates customer-facing messages.

- CLAIM_SUBMITTED — user submits a claim
- CLAIM_UNDER_REVIEW — admin starts reviewing
- CLAIM_APPROVED — admin approves
- CLAIM_REJECTED — admin rejects
- CLAIM_SETTLED — admin settles

If the notification service is down when an event is published, Kafka holds the event and delivers it when the service comes back up. Nothing gets lost.

---

## Notes

The docker-compose.yml is intentionally not committed since it contains API keys. Use docker-compose.example.yml as a template.

This project depends on the user-management service from my Loan Eligibility System for authentication and user verification.