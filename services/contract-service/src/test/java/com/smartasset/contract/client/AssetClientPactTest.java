package com.smartasset.contract.client;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.smartasset.clients.AssetClient;
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
        "services.asset.url=http://localhost:8888",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.cloud.openfeign.okhttp.enabled=true"
})
public class AssetClientPactTest {

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
    private AssetClient assetClient;

    @Pact(provider = "asset-service", consumer = "contract-service")
    public V4Pact getAssetPact(PactDslWithProvider builder) {
        String assetId = "123e4567-e89b-12d3-a456-426614174000";

        return builder
                .given("Asset exists")
                .uponReceiving("A request for asset by ID")
                .path("/api/v1/assets/" + assetId)
                .method("GET")
                .willRespondWith()
                .status(200)
                .body(newJsonBody(body -> {
                    body.booleanType("success", true);
                    body.stringType("message", "Operation successful");
                    body.object("data", data -> {
                        data.uuid("id", UUID.fromString(assetId));
                        data.stringType("name", "Test Asset");
                        data.stringType("status", "AVAILABLE");
                    });
                }).build())
                .toPact(V4Pact.class);
    }

    @Pact(provider = "asset-service", consumer = "contract-service")
    public V4Pact updateAssetStatusPact(PactDslWithProvider builder) {
        String assetId = "123e4567-e89b-12d3-a456-426614174000";

        return builder
                .given("Asset exists")
                .uponReceiving("A request to update asset status")
                .path("/api/v1/assets/" + assetId + "/status")
                .method("PATCH")
                .query("status=IN_USE")
                .willRespondWith()
                .status(200)
                .body(newJsonBody(body -> {
                    body.booleanType("success", true);
                    body.stringType("message", "Status updated successfully");
                }).build())
                .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(providerName = "asset-service", pactMethod = "getAssetPact", port = "8888")
    void testGetAsset(MockServer mockServer) {
        UUID assetId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        ApiResponse<Map<String, Object>> response = assetClient.getAsset(assetId);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().get("id").toString()).isEqualTo(assetId.toString());
    }

    @Test
    @PactTestFor(providerName = "asset-service", pactMethod = "updateAssetStatusPact", port = "8888")
    void testUpdateStatus(MockServer mockServer) {
        UUID assetId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        ApiResponse<Map<String, Object>> response = assetClient.updateStatus(assetId, "IN_USE");

        assertThat(response.isSuccess()).isTrue();
    }
}
