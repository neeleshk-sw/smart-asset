package com.smartasset.common.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class PaymentEvent {
    private UUID paymentId;
    private UUID contractId;
    private String eventType;
    private String status;
    private BigDecimal amount;
    private LocalDateTime timestamp;

    public PaymentEvent() {
    }

    public PaymentEvent(UUID paymentId, UUID contractId, String eventType, String status, BigDecimal amount) {
        this.paymentId = paymentId;
        this.contractId = contractId;
        this.eventType = eventType;
        this.status = status;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }

    public static PaymentEvent created(UUID paymentId, UUID contractId, BigDecimal amount) {
        return new PaymentEvent(paymentId, contractId, "PAYMENT_CREATED", "PENDING", amount);
    }

    public static PaymentEvent statusChanged(UUID paymentId, UUID contractId, String status) {
        return new PaymentEvent(paymentId, contractId, "PAYMENT_STATUS_CHANGED", status, null);
    }

    // Getters and Setters
    public UUID getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(UUID paymentId) {
        this.paymentId = paymentId;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
