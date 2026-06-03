package com.smartasset.contract.client;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.smartasset.clients.NotificationClient;
import com.smartasset.common.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
        "services.notification.url=http://localhost:8891",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.cloud.openfeign.okhttp.enabled=true"
})
public class NotificationClientPactTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("smartasset")
            .withUsername("user")
            .withPassword("password")
            .withInitScript("init-db.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private NotificationClient notificationClient;

    @Pact(provider = "notification-service", consumer = "contract-service")
    public V4Pact sendNotificationPact(PactDslWithProvider builder) {
        return builder
                .given("Ready to send notification")
                .uponReceiving("A request to send notification")
                .path("/api/v1/notifications")
                .method("POST")
                .headers("Content-Type", "application/json")
                .body(newJsonBody(body -> {
                    body.uuid("recipientId", UUID.randomUUID());
                    body.stringType("type", "CONTRACT_CREATED");
                    body.stringType("title", "Contract Update");
                    body.stringType("message", "Contract created");
                    body.object("metadata", m -> m.stringType("contractId", UUID.randomUUID().toString()));
                }).build())
                .willRespondWith()
                .status(200)
                .body(newJsonBody(body -> {
                    body.booleanType("success", true);
                    body.stringType("message", "Notification sent successfully");
                }).build())
                .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(providerName = "notification-service", pactMethod = "sendNotificationPact", port = "8891")
    void testSendNotification(MockServer mockServer) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("recipientId", UUID.randomUUID());
        notification.put("type", "CONTRACT_CREATED");
        notification.put("title", "Contract Update");
        notification.put("message", "Contract created");
        notification.put("metadata", Map.of("contractId", UUID.randomUUID().toString()));

        ApiResponse<Map<String, Object>> response = notificationClient.sendNotification(notification);

        assertThat(response.isSuccess()).isTrue();
    }
}
