package com.smartasset.orchestrator.workflow;

import com.smartasset.clients.*;
import com.smartasset.orchestrator.domain.SagaInstance;
import com.smartasset.orchestrator.service.SagaOrchestratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class LeasingSagaService {

    private static final Logger log = LoggerFactory.getLogger(LeasingSagaService.class);

    private final SagaOrchestratorService sagaService;
    private final CustomerClient customerClient;
    private final AssetClient assetClient;
    private final ContractClient contractClient;
    private final PaymentClient paymentClient;
    private final NotificationClient notificationClient;

    public LeasingSagaService(
            SagaOrchestratorService sagaService,
            CustomerClient customerClient,
            AssetClient assetClient,
            ContractClient contractClient,
            PaymentClient paymentClient,
            NotificationClient notificationClient) {
        this.sagaService = sagaService;
        this.customerClient = customerClient;
        this.assetClient = assetClient;
        this.contractClient = contractClient;
        this.paymentClient = paymentClient;
        this.notificationClient = notificationClient;
    }

    public SagaInstance executeLeasingWorkflow(LeasingRequest request) {
        log.info("Starting leasing workflow for customer {} and asset {}",
                request.getCustomerId(), request.getAssetId());

        // Step 1: Start Saga
        SagaInstance saga = sagaService.startSaga("LEASING", request);
        UUID sagaId = saga.getId();

        try {
            // Step 2: Validate Customer KYC
            log.info("Step 1: Validating customer KYC");
            customerClient.verifyKyc(request.getCustomerId());
            saga = sagaService.advanceSaga(sagaId, "CUSTOMER_VALIDATION", "ASSET_RESERVATION");

            // Step 3: Reserve Asset
            log.info("Step 2: Reserving asset");
            assetClient.updateStatus(request.getAssetId(), "RESERVED");
            saga = sagaService.advanceSaga(sagaId, "ASSET_RESERVATION", "CONTRACT_CREATION");

            // Step 4: Create Contract
            log.info("Step 3: Creating contract");
            Map<String, Object> contractData = new HashMap<>();
            contractData.put("customerId", request.getCustomerId().toString());
            contractData.put("assetId", request.getAssetId().toString());
            contractData.put("totalValue", request.getTotalValue());
            contractData.put("termMonths", request.getTermMonths());
            var contractResponse = contractClient.createContract(contractData);
            saga = sagaService.advanceSaga(sagaId, "CONTRACT_CREATION", "PAYMENT_SCHEDULE");

            // Step 5: Generate Payment Schedule
            log.info("Step 4: Generating payment schedule");
            // Extract contract ID from response (simplified)
            UUID contractId = UUID.randomUUID(); // In real impl, extract from response
            paymentClient.generateSchedule(contractId, request.getTotalValue(),
                    request.getTermMonths(), LocalDate.now());
            saga = sagaService.advanceSaga(sagaId, "PAYMENT_SCHEDULE", "NOTIFICATION");

            // Step 6: Send Notification
            log.info("Step 5: Sending notification");
            Map<String, Object> notification = new HashMap<>();
            notification.put("recipientId", request.getCustomerId().toString());
            notification.put("type", "EMAIL");
            notification.put("subject", "Leasing Contract Created");
            notification.put("content", "Your leasing contract has been successfully created.");
            notificationClient.sendNotification(notification);

            // Complete Saga
            sagaService.completeSaga(sagaId);
            log.info("Leasing workflow completed successfully for saga {}", sagaId);

            return saga;

        } catch (Exception e) {
            log.error("Leasing workflow failed: {}", e.getMessage());
            sagaService.failSaga(sagaId, e.getMessage());
            executeCompensation(request, saga.getCurrentStep());
            throw new RuntimeException("Leasing workflow failed", e);
        }
    }

    private void executeCompensation(LeasingRequest request, String failedStep) {
        log.info("Executing compensation from step: {}", failedStep);
        try {
            // Compensation: Release asset if reserved
            if ("CONTRACT_CREATION".equals(failedStep) || "PAYMENT_SCHEDULE".equals(failedStep)) {
                assetClient.updateStatus(request.getAssetId(), "AVAILABLE");
                log.info("Compensation: Asset released");
            }
        } catch (Exception e) {
            log.error("Compensation failed: {}", e.getMessage());
        }
    }
}
