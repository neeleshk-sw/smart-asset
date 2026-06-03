package com.smartasset.orchestrator.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "saga_instance")
public class SagaInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String sagaType;

    @Enumerated(EnumType.STRING)
    private SagaStatus status;

    private String currentStep;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String payload;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "saga_instance_steps", joinColumns = @JoinColumn(name = "saga_instance_id"))
    private List<SagaStep> steps = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public SagaInstance() {}
    
    public SagaInstance(UUID id, String sagaType, SagaStatus status, String currentStep, String payload, List<SagaStep> steps, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.sagaType = sagaType;
        this.status = status;
        this.currentStep = currentStep;
        this.payload = payload;
        this.steps = steps != null ? steps : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public static Builder builder() {
        return new Builder();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getSagaType() { return sagaType; }
    public void setSagaType(String sagaType) { this.sagaType = sagaType; }

    public SagaStatus getStatus() { return status; }
    public void setStatus(SagaStatus status) { this.status = status; }

    public String getCurrentStep() { return currentStep; }
    public void setCurrentStep(String currentStep) { this.currentStep = currentStep; }

    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }

    public List<SagaStep> getSteps() { return steps; }
    public void setSteps(List<SagaStep> steps) { this.steps = steps; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public enum SagaStatus {
        STARTED,
        COMPLETED,
        ABORTED,
        FAILED
    }
    
    public static class Builder {
        private UUID id;
        private String sagaType;
        private SagaStatus status;
        private String currentStep;
        private String payload;
        private List<SagaStep> steps;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        public Builder id(UUID id) { this.id = id; return this; }
        public Builder sagaType(String sagaType) { this.sagaType = sagaType; return this; }
        public Builder status(SagaStatus status) { this.status = status; return this; }
        public Builder currentStep(String currentStep) { this.currentStep = currentStep; return this; }
        public Builder payload(String payload) { this.payload = payload; return this; }
        public Builder steps(List<SagaStep> steps) { this.steps = steps; return this; }
        public Builder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        
        public SagaInstance build() {
            return new SagaInstance(id, sagaType, status, currentStep, payload, steps, createdAt, updatedAt);
        }
    }
}
