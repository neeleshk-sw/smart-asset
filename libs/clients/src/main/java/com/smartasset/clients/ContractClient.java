package com.smartasset.clients;

import com.smartasset.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "contract-service", url = "${services.contract.url:http://localhost:8084}")
public interface ContractClient {

    @GetMapping("/api/v1/contracts/{id}")
    ApiResponse<Map<String, Object>> getContract(@PathVariable UUID id);

    @PostMapping("/api/v1/contracts")
    ApiResponse<Map<String, Object>> createContract(@RequestBody Map<String, Object> contract);

    @PostMapping("/api/v1/contracts/{id}/approve")
    ApiResponse<Map<String, Object>> approveContract(@PathVariable UUID id);

    @PostMapping("/api/v1/contracts/{id}/activate")
    ApiResponse<Map<String, Object>> activateContract(@PathVariable UUID id);

    @PostMapping("/api/v1/contracts/{id}/cancel")
    ApiResponse<Map<String, Object>> cancelContract(@PathVariable UUID id);
}
