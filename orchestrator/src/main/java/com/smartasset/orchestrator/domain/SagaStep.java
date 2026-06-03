package com.smartasset.orchestrator.domain;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.LocalDateTime;

@Embeddable
public class SagaStep {
    private String stepName;
    
    @Enumerated(EnumType.STRING)
    private StepStatus status;
    
    private String failureReason;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public SagaStep() {}

    public SagaStep(String stepName, StepStatus status, String failureReason, LocalDateTime startedAt, LocalDateTime completedAt) {
        this.stepName = stepName;
        this.status = status;
        this.failureReason = failureReason;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
    }
    
    public static Builder builder() {
        return new Builder();
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public StepStatus getStatus() {
        return status;
    }

    public void setStatus(StepStatus status) {
        this.status = status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public enum StepStatus {
        PENDING,
        STARTED,
        COMPLETED,
        FAILED,
        COMPENSATED
    }
    
    public static class Builder {
        private String stepName;
        private StepStatus status;
        private String failureReason;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        
        public Builder stepName(String stepName) { this.stepName = stepName; return this; }
        public Builder status(StepStatus status) { this.status = status; return this; }
        public Builder failureReason(String failureReason) { this.failureReason = failureReason; return this; }
        public Builder startedAt(LocalDateTime startedAt) { this.startedAt = startedAt; return this; }
        public Builder completedAt(LocalDateTime completedAt) { this.completedAt = completedAt; return this; }
        
        public SagaStep build() {
            return new SagaStep(stepName, status, failureReason, startedAt, completedAt);
        }
    }
}
