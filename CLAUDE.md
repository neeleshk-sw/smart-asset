# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

SmartAsset is an Asset Leasing & Financing platform built as a Spring Boot 3.2 / Java 21
multi-module Maven monorepo. It consists of 12 domain microservices, an API gateway, and a
saga orchestrator, all coordinated through a shared parent `pom.xml`.

## Build & Test Commands

All commands run from the repo root (`project/`). The root `pom.xml` is the Maven reactor.

```bash
# Build everything
mvn clean install

# Build one module + its internal dependencies (mirrors CI)
mvn -pl services/asset-service -am clean verify

# Run all tests for one module
mvn -pl services/asset-service test

# Run a single test class
mvn -pl services/asset-service test -Dtest=AssetServiceTest

# Run a single test method
mvn -pl services/asset-service test -Dtest=AssetServiceTest#findById_ShouldReturnAsset

# Run a single service locally (after dependencies are up — see below)
mvn -pl services/asset-service spring-boot:run
```

`*Test.java` = unit tests (Mockito). `*IT.java` = integration tests using Testcontainers
(spin up real Postgres `postgres:15-alpine` with an `init-db.sql` init script); these require
a running Docker daemon.

### Local infrastructure

Backing services (Postgres, Kafka, Keycloak, RabbitMQ, Redis, MinIO, Elasticsearch, and the
observability stack) come from Docker Compose:

```bash
docker compose -f infrastructure/docker-compose/local-dev.yml up -d
```

Ports, schema/credential mappings, and infra connection strings are documented in
`RESOURCES_AND_PORTS.md` — consult it rather than guessing. Note: CI references a
`Dockerfile` per service for image builds, but those Dockerfiles are not yet committed.

## Architecture

### Module layout

- `libs/` — shared libraries, depended on by services via `com.smartasset` artifacts:
  - `common-dto` — the `ApiResponse<T>` response envelope (see below).
  - `common-events` — Kafka event POJOs (`AssetEvent`, `CustomerEvent`, etc.) with static
    factory methods (e.g. `AssetEvent.created(id)`).
  - `common-utils` — `GlobalExceptionHandler`, exception hierarchy
    (`SmartAssetException` → `ResourceNotFoundException`/`BadRequestException`), and the
    shared YAML config fragments under `src/main/resources`.
  - `clients` — OpenFeign client interfaces (`AssetClient`, `CustomerClient`, …) used for
    synchronous inter-service calls, primarily by the orchestrator.
- `gateway/` — Spring Cloud Gateway (reactive). Routes `/api/v1/<domain>/**` to the matching
  service by port and enforces OAuth2 JWT auth (`SecurityConfig`). `/api/v1/auth/**` and
  `/actuator/**` are public.
- `orchestrator/` — saga workflow coordinator (see saga pattern below).
- `services/<name>-service/` — 12 domain services, each a near-identical layered Spring Boot app:
  `controller → service → repository (Spring Data JPA) → domain`.

### Per-service conventions

Each service follows the asset-service template:
- Base package `com.smartasset.<domain>`; entry point `<Domain>ServiceApplication`.
- REST controllers under `/api/v1/<domain>` returning `ResponseEntity<ApiResponse<T>>`.
- Service layer is `@Transactional` for writes and publishes a Kafka event after state
  changes (e.g. `AssetService.create` sends to topic `asset-events`).
- `application.yml` sets the port + DB schema, then imports the shared fragments:
  `spring.config.import: classpath:application-common.yml, classpath:application-services.yml`.
  `application-common.yml` (in `common-utils`) holds Postgres/Kafka/RabbitMQ/actuator/tracing
  defaults; `application-services.yml` holds all inter-service URLs, Keycloak, MinIO, ES config.

### Shared response & error contract

Every endpoint returns `ApiResponse<T>` (`success`, `message`, `data`, `errorCode`, `traceId`,
`timestamp`). Construct via the static factories `ApiResponse.success(...)` /
`ApiResponse.error(...)` — do not `new` it directly in controllers. `GlobalExceptionHandler`
(`@RestControllerAdvice` in `common-utils`) maps exceptions to `ApiResponse.error` and injects
the current Micrometer `traceId`. Throw `ResourceNotFoundException` / `BadRequestException`
from the service layer rather than building error responses by hand.

> Component-scan caveat: services use the default `@SpringBootApplication` scan rooted at
> `com.smartasset.<domain>`, but shared beans (`GlobalExceptionHandler`) live under
> `com.smartasset.common`. If a service needs those shared beans wired, it must broaden the
> scan (e.g. `scanBasePackages = "com.smartasset"`) — there is currently no auto-configuration
> registering them.

### Inter-service communication

Two mechanisms coexist:
- **Asynchronous (events):** services publish domain events to Kafka topics
  (`<domain>-events`) via `KafkaTemplate`. JSON serialization is configured in
  `application-common.yml`.
- **Synchronous (Feign):** the orchestrator calls services through the Feign interfaces in
  `libs/clients`. Feign is enabled via `@EnableFeignClients(basePackages = "com.smartasset.clients")`
  on `OrchestratorApplication`; client URLs resolve from the `services.*.url` properties.

### Saga orchestration

The orchestrator implements the **orchestration-based saga** pattern for cross-service
workflows. `SagaOrchestratorService` persists `SagaInstance`/`SagaStep` state (JPA) and exposes
`startSaga` / `advanceSaga` / `completeSaga` / `failSaga`. Concrete workflows like
`LeasingSagaService.executeLeasingWorkflow` drive the step sequence by calling Feign clients
(validate KYC → reserve asset → create contract → generate payment schedule → notify) and run
compensating actions (e.g. release a reserved asset) in the `catch` block on failure. Entry
point: `POST /api/v1/workflows/leasing` (`WorkflowController`).

### Observability

All services include Actuator + Micrometer tracing (OTLP) + Prometheus registry + Loki logback
appender (wired in the root `pom.xml` as compile-scope dependencies, so every module gets them).
Metrics at `/actuator/prometheus`, health at `/actuator/health`, and a `traceId` propagates
through logs and into `ApiResponse`.

## CI/CD

`.github/workflows/main.yml` fans out per service to the reusable
`.github/workflows/service-pipeline.yml`, which runs `mvn -pl <path> -am clean verify`, a Trivy
image scan, a Docker build/push, and a Helm deploy. Helm charts live in
`infrastructure/helm/charts/<service>` with a shared `smartasset-common` chart; raw K8s
manifests are under `infrastructure/kubernetes/`.
