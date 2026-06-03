package com.smartasset.notification.controller;

import com.smartasset.notification.domain.Notification;
import com.smartasset.notification.service.NotificationService;
import com.smartasset.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Notification>>> getAllNotifications() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Notification>> getNotificationById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Notification>> sendNotification(@RequestBody Notification notification) {
        Notification sent = notificationService.send(notification);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Notification sent", sent));
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<ApiResponse<Notification>> retryNotification(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Notification retry queued", notificationService.retry(id)));
    }
}
