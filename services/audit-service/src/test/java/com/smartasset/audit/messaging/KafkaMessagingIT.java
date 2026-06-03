package com.smartasset.audit.messaging;

import com.smartasset.audit.domain.AuditLog;
import com.smartasset.audit.repository.AuditLogRepository;
import com.smartasset.common.events.AssetEvent;
import com.smartasset.common.events.CustomerEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@Testcontainers
public class KafkaMessagingIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("smartasset")
            .withUsername("user")
            .withPassword("password")
            .withInitScript("init-db.sql");

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.kafka.consumer.properties.spring.json.trusted.packages", () -> "*");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Test
    void consumeAssetEvent_ShouldCreateAuditLog() {
        UUID assetId = UUID.randomUUID();
        AssetEvent event = new AssetEvent();
        event.setAssetId(assetId);
        event.setEventType("ASSET_CREATED");
        event.setPreviousStatus(null);
        event.setNewStatus("AVAILABLE");

        kafkaTemplate.send("asset-events", event);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityId("ASSET", assetId);
            assertThat(logs).isNotEmpty();
            assertThat(logs.get(0).getAction()).isEqualTo("ASSET_CREATED");
        });
    }

    @Test
    void consumeCustomerEvent_ShouldCreateAuditLog() {
        UUID customerId = UUID.randomUUID();
        CustomerEvent event = new CustomerEvent();
        event.setCustomerId(customerId);
        event.setEventType("CUSTOMER_CREATED");
        event.setKycStatus("PENDING");

        kafkaTemplate.send("customer-events", event);

        await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            List<AuditLog> logs = auditLogRepository.findByEntityTypeAndEntityId("CUSTOMER", customerId);
            assertThat(logs).isNotEmpty();
            assertThat(logs.get(0).getAction()).isEqualTo("CUSTOMER_CREATED");
        });
    }
}
