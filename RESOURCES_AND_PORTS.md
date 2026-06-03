# SmartAsset Platform: Ports and Resources Reference

This document provides a comprehensive list of ports and infrastructure resources used by the SmartAsset microservices platform.

## Microservices Ports

| Service Name | Port | Description |
| :--- | :--- | :--- |
| **Gateway** | `8080` | Entry point for all external traffic. |
| **Orchestrator** | `8081` | Saga workflow management and coordination. |
| **Asset Service** | `8082` | Managing physical and digital assets. |
| **Customer Service** | `8083` | Customer profiles and KYC management. |
| **Contract Service** | `8084` | Lease contracts and legal documents. |
| **Payment Service** | `8085` | Payment scheduling and processing. |
| **Document Service** | `8086` | Document storage and management. |
| **Notification Service** | `8087` | Email and push notifications. |
| **Maintenance Service** | `8088` | Asset maintenance and repair tracking. |
| **Billing Service** | `8089` | Invoicing and billing operations. |
| **Audit Service** | `8090` | Centralized system audit logs. |
| **Search Service** | `8091` | Global search across assets and customers (Elasticsearch). |
| **User Service** | `8092` | Internal user and role management. |

## Infrastructure Resources

| Resource | Port | Default Value / Connection String |
| :--- | :--- | :--- |
| **PostgreSQL** | `5432` | `jdbc:postgresql://localhost:5432/smartasset` |
| **Kafka** | `9092` | `localhost:9092` |
| **RabbitMQ** | `5672` | `localhost:5672` (Management UI on `15672`) |
| **Keycloak** | `8081` | `http://localhost:8081/realms/smartasset` (Local Dev) |
| **Loki** | `3100` | `http://localhost:3100` |
| **Prometheus** | `9090` | `http://localhost:9090` |
| **Grafana** | `3000` | `http://localhost:3000` |

### Database Details
Shared PostgreSQL instance (`smartasset` database) with service-specific schemas.

| Schema | Username | Password |
| :--- | :--- | :--- |
| `asset_schema` | `asset_user` | `asset@123` |
| `customer_schema` | `customer_user` | `customer@123` |
| `contract_schema` | `contract_user` | `contract@123` |
| `payment_schema` | `payment_user` | `payment@123` |
| `orchestrator_schema` | `orchestrator_user` | `orchestrator@123` |
| `audit_schema` | `audit_user` | `audit@123` |
| `notification_schema` | `notification_user` | `notification@123` |
| `maintenance_schema` | `maintenance_user` | `maintenance@123` |
| `billing_schema` | `billing_user` | `billing@123` |
| `document_schema` | `document_user` | `document@123` |
| `search_schema` | `search_user` | `search@123` |
| `user_schema` | `user_service_user` | `user@123` |

> [!TIP]
> Use these credentials for local database management and tool configuration.

## Distributed Tracing & Metrics

Every service is equipped with Spring Boot Actuator and Micrometer:
- **Metrics Path**: `/actuator/prometheus`
- **Health Check**: `/actuator/health`
- **Trace Correlation**: All logs and responses include a `traceId`.
