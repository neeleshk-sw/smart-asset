package com.smartasset.contract.client;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.smartasset.clients.CustomerClient;
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

import java.util.Map;
import java.util.UUID;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {
        "services.customer.url=http://localhost:8889",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.cloud.openfeign.okhttp.enabled=true"
})
public class CustomerClientPactTest {

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
    private CustomerClient customerClient;

    @Pact(provider = "customer-service", consumer = "contract-service")
    public V4Pact getCustomerPact(PactDslWithProvider builder) {
        String customerId = "123e4567-e89b-12d3-a456-426614174000";

        return builder
                .given("Customer exists")
                .uponReceiving("A request for customer by ID")
                .path("/api/v1/customers/" + customerId)
                .method("GET")
                .willRespondWith()
                .status(200)
                .body(newJsonBody(body -> {
                    body.booleanType("success", true);
                    body.stringType("message", "Operation successful");
                    body.object("data", data -> {
                        data.uuid("id", UUID.fromString(customerId));
                        data.stringType("firstName", "John");
                        data.stringType("lastName", "Doe");
                        data.stringType("email", "john.doe@example.com");
                    });
                }).build())
                .toPact(V4Pact.class);
    }

    @Pact(provider = "customer-service", consumer = "contract-service")
    public V4Pact verifyKycPact(PactDslWithProvider builder) {
        String customerId = "123e4567-e89b-12d3-a456-426614174000";

        return builder
                .given("Customer exists")
                .uponReceiving("A request to verify KYC")
                .path("/api/v1/customers/" + customerId + "/verify-kyc")
                .method("POST")
                .willRespondWith()
                .status(200)
                .body(newJsonBody(body -> {
                    body.booleanType("success", true);
                    body.stringType("message", "KYC verified successfully");
                }).build())
                .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(providerName = "customer-service", pactMethod = "getCustomerPact", port = "8889")
    void testGetCustomer(MockServer mockServer) {
        UUID customerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        ApiResponse<Map<String, Object>> response = customerClient.getCustomer(customerId);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().get("id").toString()).isEqualTo(customerId.toString());
    }

    @Test
    @PactTestFor(providerName = "customer-service", pactMethod = "verifyKycPact", port = "8889")
    void testVerifyKyc(MockServer mockServer) {
        UUID customerId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        ApiResponse<Map<String, Object>> response = customerClient.verifyKyc(customerId);

        assertThat(response.isSuccess()).isTrue();
    }
}
