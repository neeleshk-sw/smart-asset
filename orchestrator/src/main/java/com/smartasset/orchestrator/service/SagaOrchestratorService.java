package com.smartasset.orchestrator.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartasset.orchestrator.domain.SagaInstance;
import com.smartasset.orchestrator.domain.SagaStep;
import com.smartasset.orchestrator.repository.SagaInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SagaOrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(SagaOrchestratorService.class);

    private final SagaInstanceRepository sagaInstanceRepository;
    private final ObjectMapper objectMapper;
    
    public SagaOrchestratorService(SagaInstanceRepository sagaInstanceRepository, ObjectMapper objectMapper) {
        this.sagaInstanceRepository = sagaInstanceRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public SagaInstance startSaga(String type, Object payload) {
        log.info("Starting new Saga of type: {}", type);
        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize saga payload", e);
        }

        SagaInstance saga = SagaInstance.builder()
                .sagaType(type)
                .status(SagaInstance.SagaStatus.STARTED)
                .payload(payloadJson)
                .build();
        
        return sagaInstanceRepository.save(saga);
    }

    @Transactional
    public SagaInstance advanceSaga(UUID sagaId, String stepName, String nextStep) {
        SagaInstance saga = sagaInstanceRepository.findById(sagaId)
                .orElseThrow(() -> new RuntimeException("Saga not found: " + sagaId));

        log.info("Advancing Saga {} from step {} to {}", sagaId, stepName, nextStep);

        SagaStep completedStep = SagaStep.builder()
                .stepName(stepName)
                .status(SagaStep.StepStatus.COMPLETED)
                .completedAt(LocalDateTime.now())
                .build();
        
        saga.getSteps().add(completedStep);
        saga.setCurrentStep(nextStep);
        
        return sagaInstanceRepository.save(saga);
    }
    
    @Transactional
    public void completeSaga(UUID sagaId) {
        SagaInstance saga = sagaInstanceRepository.findById(sagaId)
                .orElseThrow(() -> new RuntimeException("Saga not found: " + sagaId));
        
        saga.setStatus(SagaInstance.SagaStatus.COMPLETED);
        sagaInstanceRepository.save(saga);
        log.info("Saga {} completed successfully", sagaId);
    }

    @Transactional
    public void failSaga(UUID sagaId, String reason) {
        SagaInstance saga = sagaInstanceRepository.findById(sagaId)
                .orElseThrow(() -> new RuntimeException("Saga not found: " + sagaId));
        
        saga.setStatus(SagaInstance.SagaStatus.FAILED);
        // Add failure log or trigger compensation logic here
        
        sagaInstanceRepository.save(saga);
        log.error("Saga {} failed: {}", sagaId, reason);
    }
}
