# SmartAsset Platform

Asset Leasing & Financing Platform — a Spring Boot 3.2 / Java 21 microservices monorepo.

## Architecture Overview

The platform consists of 12 domain microservices, an API gateway, and a saga orchestrator, coordinated through a shared Maven reactor (`pom.xml`).

```
project/
├── libs/                   # Shared libraries
│   ├── common-dto          # ApiResponse<T> envelope
│   ├── common-events       # Kafka event POJOs
│   ├── common-utils        # GlobalExceptionHandler, exception hierarchy, shared YAML config
│   └── clients             # OpenFeign client interfaces for inter-service calls
├── gateway/                # Spring Cloud Gateway — OAuth2 JWT, routes all /api/v1/** traffic
├── orchestrator/           # Saga workflow coordinator (leasing, KYC, payment flows)
├── services/               # 12 domain microservices
│   ├── asset-service       # :8082 — physical and digital asset management
│   ├── customer-service    # :8083 — customer profiles and KYC
│   ├── contract-service    # :8084 — lease contracts
│   ├── payment-service     # :8085 — payment scheduling and processing
│   ├── document-service    # :8086 — document storage (MinIO)
│   ├── notification-service # :8087 — email and push notifications
│   ├── maintenance-service # :8088 — asset maintenance tracking
│   ├── billing-service     # :8089 — invoicing and billing
│   ├── audit-service       # :8090 — centralized audit logs
│   ├── search-service      # :8091 — Elasticsearch-backed global search
│   └── user-service        # :8092 — internal user and role management
└── infrastructure/         # Docker Compose, Helm charts, Kubernetes manifests
```

## Prerequisites

- Java 21
- Maven 3.9+
- Docker Desktop (for integration tests and local dev)

## Quick Start

### 1. Start local infrastructure

```bash
docker compose -f infrastructure/docker-compose/local-dev.yml up -d
```

This starts Postgres, Kafka, Keycloak, RabbitMQ, Redis, MinIO, Elasticsearch, and the observability stack (Loki, Prometheus, Grafana).

### 2. Build the platform

```bash
mvn clean install
```

### 3. Run a service

```bash
mvn -pl services/asset-service spring-boot:run
```

The gateway on `:8080` routes all external traffic. See [RESOURCES_AND_PORTS.md](RESOURCES_AND_PORTS.md) for all ports and credentials.

## Build & Test Commands

```bash
# Build everything
mvn clean install

# Build one module + its dependencies (mirrors CI)
mvn -pl services/asset-service -am clean verify

# Run unit tests for one module
mvn -pl services/asset-service test

# Run a single test class
mvn -pl services/asset-service test -Dtest=AssetServiceTest

# Run a single test method
mvn -pl services/asset-service test -Dtest=AssetServiceTest#findById_ShouldReturnAsset

# Skip tests (compile only)
mvn clean install -DskipTests
```

`*Test.java` = unit tests (Mockito). `*IT.java` = integration tests using Testcontainers (require Docker).

## Key Design Decisions

### Response envelope
Every endpoint returns `ApiResponse<T>` (`success`, `message`, `data`, `errorCode`, `traceId`, `timestamp`). Construct via `ApiResponse.success(...)` / `ApiResponse.error(...)`.

### Inter-service communication
- **Async (Kafka):** domain events on `<domain>-events` topics after every state change.
- **Sync (Feign):** orchestrator calls services via Feign interfaces in `libs/clients`.

### Saga orchestration
`POST /api/v1/workflows/leasing` triggers the leasing saga: validate KYC → reserve asset → create contract → generate payment schedule → notify. Compensating transactions run on failure.

### Observability
All services expose `/actuator/health`, `/actuator/prometheus`, and propagate a `traceId` through logs and API responses via Micrometer + OTLP.

## Infrastructure Ports

| Service | Port |
|---------|------|
| Gateway | 8080 |
| Postgres | 5432 |
| Kafka | 9092 |
| Keycloak | 8081 |
| Grafana | 3000 |
| Prometheus | 9090 |

Full port and credential reference: [RESOURCES_AND_PORTS.md](RESOURCES_AND_PORTS.md)

## CI/CD

`.github/workflows/main.yml` fans out per service to a reusable pipeline: `mvn verify` → Trivy image scan → Docker build/push → Helm deploy. Helm charts are in `infrastructure/helm/charts/<service>`.
