package com.smartasset.common.events;

import java.time.LocalDateTime;
import java.util.UUID;

public class AssetEvent {
    private UUID assetId;
    private String eventType;
    private String previousStatus;
    private String newStatus;
    private LocalDateTime timestamp;

    public AssetEvent() {
    }

    public AssetEvent(UUID assetId, String eventType, String previousStatus, String newStatus) {
        this.assetId = assetId;
        this.eventType = eventType;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.timestamp = LocalDateTime.now();
    }

    public static AssetEvent created(UUID assetId) {
        return new AssetEvent(assetId, "ASSET_CREATED", null, "AVAILABLE");
    }

    public static AssetEvent statusChanged(UUID assetId, String previousStatus, String newStatus) {
        return new AssetEvent(assetId, "ASSET_STATUS_CHANGED", previousStatus, newStatus);
    }

    // Getters and Setters
    public UUID getAssetId() {
        return assetId;
    }

    public void setAssetId(UUID assetId) {
        this.assetId = assetId;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
