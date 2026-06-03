package com.smartasset.contract.service;

import com.smartasset.clients.AssetClient;
import com.smartasset.clients.CustomerClient;
import com.smartasset.clients.NotificationClient;
import com.smartasset.common.dto.ApiResponse;
import com.smartasset.common.events.ContractEvent;
import com.smartasset.common.exception.BadRequestException;
import com.smartasset.common.exception.ResourceNotFoundException;
import com.smartasset.contract.domain.Contract;
import com.smartasset.contract.repository.ContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private CustomerClient customerClient;

    @Mock
    private AssetClient assetClient;

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private KafkaTemplate<String, ContractEvent> kafkaTemplate;

    @InjectMocks
    private ContractService contractService;

    @BeforeEach
    void setUp() {
        // Mockito's @InjectMocks will handle the instantiation and injection
    }

    @Test
    void findAll_ShouldReturnList() {
        when(contractRepository.findAll()).thenReturn(Arrays.asList(new Contract(), new Contract()));
        assertThat(contractService.findAll()).hasSize(2);
    }

    @Test
    void create_ShouldValidateAndSave() {
        UUID customerId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        Contract contract = new Contract();
        contract.setCustomerId(customerId);
        contract.setAssetId(assetId);

        when(customerClient.getCustomer(customerId))
                .thenReturn(ApiResponse.success("OK", Collections.emptyMap()));
        when(assetClient.getAsset(assetId))
                .thenReturn(ApiResponse.success("OK", Collections.emptyMap()));
        when(contractRepository.save(any(Contract.class))).thenAnswer(i -> {
            Contract c = i.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        Contract result = contractService.create(contract);

        assertThat(result.getStatus()).isEqualTo(Contract.ContractStatus.DRAFT);
        verify(customerClient).getCustomer(customerId);
        verify(assetClient).getAsset(assetId);
        verify(contractRepository).save(contract);
        verify(notificationClient).sendNotification(any());
    }

    @Test
    void create_WhenCustomerNotFound_ShouldThrowException() {
        UUID customerId = UUID.randomUUID();
        Contract contract = new Contract();
        contract.setCustomerId(customerId);

        when(customerClient.getCustomer(customerId)).thenThrow(new RuntimeException("Not Found"));

        assertThatThrownBy(() -> contractService.create(contract))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Customer validation failed");
    }

    @Test
    void submitForApproval_WhenDraft_ShouldUpdateStatus() {
        UUID id = UUID.randomUUID();
        Contract contract = new Contract();
        contract.setId(id);
        contract.setStatus(Contract.ContractStatus.DRAFT);

        when(contractRepository.findById(id)).thenReturn(Optional.of(contract));
        when(contractRepository.save(any(Contract.class))).thenReturn(contract);

        Contract result = contractService.submitForApproval(id);

        assertThat(result.getStatus()).isEqualTo(Contract.ContractStatus.PENDING_APPROVAL);
        verify(notificationClient).sendNotification(any());
    }

    @Test
    void submitForApproval_WhenNotDraft_ShouldThrowException() {
        UUID id = UUID.randomUUID();
        Contract contract = new Contract();
        contract.setStatus(Contract.ContractStatus.ACTIVE);

        when(contractRepository.findById(id)).thenReturn(Optional.of(contract));

        assertThatThrownBy(() -> contractService.submitForApproval(id))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void activate_WhenApproved_ShouldReserveAssetAndActivate() {
        UUID id = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        Contract contract = new Contract();
        contract.setId(id);
        contract.setAssetId(assetId);
        contract.setStatus(Contract.ContractStatus.APPROVED);

        when(contractRepository.findById(id)).thenReturn(Optional.of(contract));
        when(contractRepository.save(any(Contract.class))).thenReturn(contract);

        Contract result = contractService.activate(id);

        assertThat(result.getStatus()).isEqualTo(Contract.ContractStatus.ACTIVE);
        verify(assetClient).updateStatus(assetId, "RESERVED");
        verify(notificationClient).sendNotification(any());
    }

    @Test
    void cancel_WhenActive_ShouldReleaseAssetAndCancel() {
        UUID id = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        Contract contract = new Contract();
        contract.setId(id);
        contract.setAssetId(assetId);
        contract.setStatus(Contract.ContractStatus.ACTIVE);

        when(contractRepository.findById(id)).thenReturn(Optional.of(contract));
        when(contractRepository.save(any(Contract.class))).thenReturn(contract);

        Contract result = contractService.cancel(id);

        assertThat(result.getStatus()).isEqualTo(Contract.ContractStatus.CANCELLED);
        verify(assetClient).updateStatus(assetId, "AVAILABLE");
        verify(notificationClient).sendNotification(any());
    }
}
