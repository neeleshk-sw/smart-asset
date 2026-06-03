package com.smartasset.clients;

import com.smartasset.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "customer-service", url = "${services.customer.url:http://localhost:8083}")
public interface CustomerClient {

    @GetMapping("/api/v1/customers/{id}")
    ApiResponse<Map<String, Object>> getCustomer(@PathVariable UUID id);

    @PostMapping("/api/v1/customers/{id}/verify-kyc")
    ApiResponse<Map<String, Object>> verifyKyc(@PathVariable UUID id);
}
