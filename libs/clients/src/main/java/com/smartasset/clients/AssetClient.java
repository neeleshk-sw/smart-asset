package com.smartasset.clients;

import com.smartasset.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@FeignClient(name = "asset-service", url = "${services.asset.url:http://localhost:8082}")
public interface AssetClient {

    @GetMapping("/api/v1/assets/{id}")
    ApiResponse<Map<String, Object>> getAsset(@PathVariable UUID id);

    @PatchMapping("/api/v1/assets/{id}/status")
    ApiResponse<Map<String, Object>> updateStatus(@PathVariable UUID id, @RequestParam String status);
}
