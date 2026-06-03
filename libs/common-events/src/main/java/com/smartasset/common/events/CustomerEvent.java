package com.smartasset.common.events;

import java.time.LocalDateTime;
import java.util.UUID;

public class CustomerEvent {
    private UUID customerId;
    private String eventType;
    private String kycStatus;
    private LocalDateTime timestamp;

    public CustomerEvent() {
    }

    public CustomerEvent(UUID customerId, String eventType, String kycStatus) {
        this.customerId = customerId;
        this.eventType = eventType;
        this.kycStatus = kycStatus;
        this.timestamp = LocalDateTime.now();
    }

    public static CustomerEvent created(UUID customerId) {
        return new CustomerEvent(customerId, "CUSTOMER_CREATED", "PENDING");
    }

    public static CustomerEvent verified(UUID customerId) {
        return new CustomerEvent(customerId, "CUSTOMER_VERIFIED", "VERIFIED");
    }

    // Getters and Setters
    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getKycStatus() {
        return kycStatus;
    }

    public void setKycStatus(String kycStatus) {
        this.kycStatus = kycStatus;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
