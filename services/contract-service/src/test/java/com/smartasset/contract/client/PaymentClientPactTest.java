package com.smartasset.contract.client;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.smartasset.clients.PaymentClient;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
        "services.payment.url=http://localhost:8890",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.cloud.openfeign.okhttp.enabled=true"
})
public class PaymentClientPactTest {

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
    private PaymentClient paymentClient;

    @Pact(provider = "payment-service", consumer = "contract-service")
    public V4Pact generateSchedulePact(PactDslWithProvider builder) {
        UUID contractId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        return builder
                .given("Ready to generate schedule")
                .uponReceiving("A request to generate payment schedule")
                .path("/api/v1/payments/generate-schedule")
                .method("POST")
                .query("contractId=" + contractId + "&totalAmount=1000.0&termMonths=12&startDate=2024-01-01")
                .willRespondWith()
                .status(200)
                .body(newJsonBody(body -> {
                    body.booleanType("success", true);
                    body.stringType("message", "Schedule generated successfully");
                    body.minArrayLike("data", 1, item -> {
                        item.uuid("id");
                        item.decimalType("amount", 83.33);
                        item.date("dueDate", "yyyy-MM-dd");
                        item.stringType("status", "PENDING");
                    });
                }).build())
                .toPact(V4Pact.class);
    }

    @Pact(provider = "payment-service", consumer = "contract-service")
    public V4Pact getPaymentsByContractPact(PactDslWithProvider builder) {
        UUID contractId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

        return builder
                .given("Payments exist for contract")
                .uponReceiving("A request for payments by contract ID")
                .path("/api/v1/payments/contract/" + contractId)
                .method("GET")
                .willRespondWith()
                .status(200)
                .body(newJsonBody(body -> {
                    body.booleanType("success", true);
                    body.minArrayLike("data", 1, item -> {
                        item.uuid("id");
                        item.decimalType("amount", 100.0);
                        item.stringType("status", "PAID");
                    });
                }).build())
                .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(providerName = "payment-service", pactMethod = "generateSchedulePact", port = "8890")
    void testGenerateSchedule(MockServer mockServer) {
        UUID contractId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        BigDecimal totalAmount = new BigDecimal("1000.0");
        int termMonths = 12;
        LocalDate startDate = LocalDate.of(2024, 1, 1);

        ApiResponse<List<Map<String, Object>>> response = paymentClient.generateSchedule(contractId, totalAmount,
                termMonths, startDate);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotEmpty();
    }

    @Test
    @PactTestFor(providerName = "payment-service", pactMethod = "getPaymentsByContractPact", port = "8890")
    void testGetPaymentsByContract(MockServer mockServer) {
        UUID contractId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        ApiResponse<List<Map<String, Object>>> response = paymentClient.getPaymentsByContract(contractId);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).isNotEmpty();
    }
}
