package com.smartasset.common.events;

import java.time.LocalDateTime;
import java.util.UUID;

public class ContractEvent {
    private UUID contractId;
    private String eventType;
    private String previousStatus;
    private String newStatus;
    private String details;
    private LocalDateTime timestamp;

    public ContractEvent() {
    }

    public ContractEvent(UUID contractId, String eventType, String previousStatus, String newStatus, String details) {
        this.contractId = contractId;
        this.eventType = eventType;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    public static ContractEvent created(UUID contractId) {
        return new ContractEvent(contractId, "CONTRACT_CREATED", null, "DRAFT", "New contract created");
    }

    public static ContractEvent statusChanged(UUID contractId, String previousStatus, String newStatus,
            String details) {
        return new ContractEvent(contractId, "CONTRACT_STATUS_CHANGED", previousStatus, newStatus, details);
    }

    // Getters and Setters
    public UUID getContractId() {
        return contractId;
    }

    public void setContractId(UUID contractId) {
        this.contractId = contractId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getPreviousStatus() {
        return previousStatus;
    }

    public void setPreviousStatus(String previousStatus) {
        this.previousStatus = previousStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
