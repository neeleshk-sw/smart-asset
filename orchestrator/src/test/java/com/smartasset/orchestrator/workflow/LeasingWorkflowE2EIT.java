package com.smartasset.orchestrator.workflow;

import com.smartasset.clients.*;
import com.smartasset.common.dto.ApiResponse;
import com.smartasset.orchestrator.domain.SagaInstance;
import com.smartasset.orchestrator.repository.SagaInstanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class LeasingWorkflowE2EIT {

    @Autowired
    private LeasingSagaService leasingSagaService;

    @Autowired
    private SagaInstanceRepository sagaRepository;

    @MockBean
    private CustomerClient customerClient;

    @MockBean
    private AssetClient assetClient;

    @MockBean
    private ContractClient contractClient;

    @MockBean
    private PaymentClient paymentClient;

    @MockBean
    private NotificationClient notificationClient;

    private LeasingRequest testRequest;
    private UUID customerId;
    private UUID assetId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        assetId = UUID.randomUUID();

        testRequest = new LeasingRequest();
        testRequest.setCustomerId(customerId);
        testRequest.setAssetId(assetId);
        testRequest.setTotalValue(new BigDecimal("50000"));
        testRequest.setTermMonths(36);

        // Default successful mocks
        when(customerClient.verifyKyc(any())).thenReturn(ApiResponse.success(null));
        when(assetClient.updateStatus(any(), anyString())).thenReturn(ApiResponse.success(null));
        when(contractClient.createContract(any())).thenReturn(ApiResponse.success(new HashMap<>()));
        when(paymentClient.generateSchedule(any(), any(), anyInt(), any())).thenReturn(ApiResponse.success(null));
        when(notificationClient.sendNotification(any())).thenReturn(ApiResponse.success(null));
    }

    @Test
    void testCompleteLeasingWorkflow_Success() {
        // Execute workflow
        SagaInstance saga = leasingSagaService.executeLeasingWorkflow(testRequest);

        // Fetch latest state from Repo
        SagaInstance latestSaga = sagaRepository.findById(saga.getId()).orElseThrow();

        // Verify Saga State
        assertNotNull(latestSaga);
        assertEquals(SagaInstance.SagaStatus.COMPLETED, latestSaga.getStatus());

        // Verify Service Interactions
        verify(customerClient).verifyKyc(customerId);
        verify(assetClient).updateStatus(assetId, "RESERVED");
        verify(contractClient).createContract(anyMap());
        verify(paymentClient).generateSchedule(any(), eq(testRequest.getTotalValue()),
                eq(testRequest.getTermMonths()), any());
        verify(notificationClient).sendNotification(anyMap());
    }

    @Test
    void testLeasingWorkflow_FailureAtContractCreation_TriggersCompensation() {
        // Mock failure at Contract Creation
        when(contractClient.createContract(any()))
                .thenThrow(new RuntimeException("Contract Service Down"));

        // Execute and expect exception
        assertThrows(RuntimeException.class, () -> leasingSagaService.executeLeasingWorkflow(testRequest));

        // Verify Saga State - find first saga (unique in this test run)
        SagaInstance saga = sagaRepository.findAll().stream()
                .filter(s -> s.getStatus() == SagaInstance.SagaStatus.FAILED)
                .findFirst()
                .orElseThrow();
        assertEquals(SagaInstance.SagaStatus.FAILED, saga.getStatus());

        // Verify Compensation: Asset status should be reset to AVAILABLE after failure
        // at Contract Creation
        verify(assetClient).updateStatus(assetId, "AVAILABLE");
    }
}
