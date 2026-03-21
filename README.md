# Agentic AI Claims Processing System

A production-grade insurance claims processing platform built on Spring Boot microservices. The system uses a RAG-powered AI agent to autonomously evaluate claims against historical fraud patterns, policy coverage rules, and regulatory compliance guidelines — making decisions without hardcoded business logic.

---

## Background

Most AI integrations in enterprise backends follow the same pattern — send text to an LLM, get a response, display it. That works for simple use cases but breaks down when you need decisions grounded in your own data, auditable reasoning, and deterministic action execution.

This project explores a more serious integration pattern: an AI agent with tool-calling capabilities, backed by a vector store populated with domain-specific knowledge, making decisions that trigger real downstream actions through Kafka. The agent doesn't just respond — it reasons, retrieves, and acts.

---

## Architecture

```
User submits claim
           │
           ▼
   ai-claims-service (port 8083)
   ├── Validates user via user-management
   ├── Groq AI fraud scoring + circuit breaker fallback
   ├── Saves claim to PostgreSQL
   ├── Publishes to Kafka → claim-events
   └── Triggers claims-decision-engine (async, fire-and-forget)
           │
           ▼
   claims-decision-engine (port 8085)
   ├── RAG Tool → pgvector similarity search (past claims)
   ├── RAG Tool → pgvector similarity search (policy documents)
   ├── RAG Tool → pgvector similarity search (compliance rules)
   ├── Groq AI reasons through all retrieved context
   └── Action Tool → auto-approve / escalate / request info / fraud report
           │
           ▼
   Kafka → claim-decisions topic
           │
           ▼
   notification-service (port 8084)
   ├── Consumes claim-events → submission email
   └── Consumes claim-decisions → decision email with AI reasoning
```

---

## Architecture decisions

**Why separate the decision engine from the claims service?**

The `ai-claims-service` existed before this project as a standalone fraud scoring service. Rather than bloating it with agent logic, vector store dependencies, and embedding infrastructure, I isolated the agentic layer into `claims-decision-engine`. This keeps the claims service focused on its core responsibility — claim lifecycle management — while the decision engine owns the AI reasoning pipeline. It also means the agent can be iterated on or swapped without touching claim processing logic.

**Why pgvector over a dedicated vector database?**

Pinecone and Weaviate are solid but they introduce operational complexity — another service to run, another API to manage, another failure point. Since PostgreSQL is already in the stack, pgvector keeps the vector store co-located with the relational data. For the scale this system targets, pgvector with an HNSW index performs well and simplifies the infrastructure considerably.

**Why Ollama for embeddings?**

Groq doesn't support embedding models. OpenAI does, but adding a second paid API dependency for embeddings felt unnecessary. Ollama runs `nomic-embed-text` locally — consistent 768-dimension vectors, no API cost, no rate limits, no external dependency at runtime.

**Why async for the decision engine call?**

The agent reasoning pipeline takes 5-10 seconds — RAG retrieval, three tool calls, LLM reasoning, Kafka publish. Blocking the claim submission response on that would degrade the user experience for no benefit, since the decision result arrives via email anyway. The `ai-claims-service` fires a non-blocking WebClient call with `.subscribe()` and returns immediately.

**Why fire-and-forget instead of a Kafka-triggered agent?**

Publishing a Kafka event from `ai-claims-service` and having the decision engine consume it would be cleaner for resilience — automatic retry, dead letter queues, replayability. That's the right long-term pattern. For this project I used a direct HTTP call to keep the setup simpler. Migrating to event-driven triggering is the obvious next step.

---

## Services

| Service | Port | Responsibility |
|---|---|---|
| user-management | 8081 | JWT auth, user lifecycle |
| ai-claims-service | 8083 | Claim submission, Groq fraud scoring, status management |
| claims-decision-engine | 8085 | Spring AI agent, RAG retrieval, decision making |
| notification-service | 8084 | Kafka consumer, transactional email |

---

## Tech stack

- Java 17, Spring Boot 3.4.5 / 3.5.11
- Spring AI 1.0.0 — agent framework, tool calling, vector store abstraction
- Groq API (llama-3.3-70b-versatile) — LLM for fraud scoring and agent reasoning
- Ollama (nomic-embed-text) — local embedding model, 768 dimensions
- pgvector on PostgreSQL — HNSW index, cosine similarity search
- Apache Kafka — async event streaming
- Redis — response caching on fraud assessments
- Resilience4j — circuit breaker and time limiter on all external AI calls
- Docker Compose — full local environment in a single command

---

## How the agent works

Spring AI exposes a `ChatClient` with tool-calling support. Tools are annotated Java methods — the `@Tool` description is what the LLM reads to decide when and how to invoke them.

```java
@Tool(description = """
        Search for similar past insurance claims based on the claim description.
        Use this to find fraud patterns and similar cases before making a decision.
        """)
public String searchSimilarClaims(String claimDescription) {
    // pgvector similarity search via Ollama embeddings
}
```

The agent prompt instructs the LLM to search similar claims, retrieve applicable policy rules, check compliance guidelines, and then call one of the action tools — `autoApproveClaim`, `escalateToUnderwriter`, `requestMoreInfo`, or `generateFraudReport`. Groq decides the order, decides which action tool fits, and Spring AI intercepts each tool call decision to execute the actual Java method.

The decision logic lives entirely in the LLM's reasoning, not in application code. Adding a new decision type means adding a new tool method and updating the prompt — no conditional branching in the service layer.

---

## RAG data

The vector store is seeded on startup with:

- 8 past claims across VEHICLE, MEDICAL, PROPERTY, and LIFE categories with labelled outcomes
- 4 policy documents with coverage limits and claim-type-specific requirements
- 5 compliance rules covering fraud indicators and mandatory escalation thresholds

All documents are embedded via Ollama and stored in pgvector with metadata. The seeder checks for existing data before running to avoid duplicates on restart.

---

## Decision examples

| Claim | Amount | Fraud Risk | Agent Decision |
|---|---|---|---|
| Vehicle rear-end collision, police report filed | ₹25,000 | LOW | Auto-approved |
| Vehicle theft, same claimant filed 6 months ago | ₹45,000 | HIGH | Escalated + fraud report |
| Medical claim, no police report, partial documents | ₹95,000 | MEDIUM | Escalated for review |
| Property fire damage, multiple witnesses | ₹1,80,000 | LOW | Escalated (exceeds ₹1,00,000 compliance threshold) |

---

## Running locally

Requires Docker Desktop, Java 17, Maven, and Ollama.

```bash
ollama pull nomic-embed-text
```

```bash
git clone https://github.com/SunilReddy93/Agentic-AI-with-RAG-Claims-Processing-System.git
cd Agentic-AI-with-RAG-Claims-Processing-System

cp docker-compose.example.yml docker-compose.yml
cp ai-claims-service/src/main/resources/application.properties.example \
   ai-claims-service/src/main/resources/application.properties
cp claims-decision-engine/src/main/resources/application.properties.example \
   claims-decision-engine/src/main/resources/application.properties
cp user-management/src/main/resources/application.properties.example \
   user-management/src/main/resources/application.properties
```

Add your Groq API key (free at console.groq.com) and Gmail app password to the config files.

```bash
mvn clean package -DskipTests
docker-compose up --build -d
```

---

## API

Auth — `user-management` on port 8081:
```
POST /api/auth/register
POST /api/auth/login
```

Claims — `ai-claims-service` on port 8083:
```
POST /api/claims/submit
GET  /api/claims/{id}
GET  /api/claims/my-claims
GET  /api/claims/{id}/history
PUT  /api/claims/{id}/approve   ADMIN
PUT  /api/claims/{id}/reject    ADMIN
PUT  /api/claims/{id}/settle    ADMIN
```

Submitting a claim triggers the full pipeline. Two emails arrive — submission confirmation from `claim-events`, decision with full agent reasoning from `claim-decisions`.

---

## Sample request

```bash
curl -X POST http://localhost:8083/api/claims/submit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your_token>" \
  -d '{
    "idempotencyKey": "claim-001",
    "claimType": "VEHICLE",
    "claimDetail": {
        "damageCode": "VEH-001",
        "damagedItem": "Car - Honda City",
        "incidentDate": "2026-03-21",
        "incidentLocation": "MG Road, Bangalore",
        "incidentDescription": "Rear end collision at traffic signal.",
        "causeType": "ARTIFICIAL",
        "estimatedAmount": 25000.00
    }
}'
```

---

## Author

Sunil Reddy — Java Backend Engineer
