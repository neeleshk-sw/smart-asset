package com.smartasset.clients;

import com.smartasset.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "notification-service", url = "${services.notification.url:http://localhost:8087}")
public interface NotificationClient {

    @PostMapping("/api/v1/notifications")
    ApiResponse<Map<String, Object>> sendNotification(@RequestBody Map<String, Object> notification);
}
