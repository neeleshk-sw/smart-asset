package com.smartasset.contract.service;

import com.smartasset.clients.NotificationClient;
import com.smartasset.clients.CustomerClient;
import com.smartasset.clients.AssetClient;
import com.smartasset.common.events.ContractEvent;
import com.smartasset.contract.domain.Contract;
import com.smartasset.contract.repository.ContractRepository;
import com.smartasset.common.exception.ResourceNotFoundException;
import com.smartasset.common.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ContractService {

    private static final Logger log = LoggerFactory.getLogger(ContractService.class);

    private final ContractRepository contractRepository;
    private final CustomerClient customerClient;
    private final AssetClient assetClient;
    private final NotificationClient notificationClient;
    private final KafkaTemplate<String, ContractEvent> kafkaTemplate;

    private static final String CONTRACT_TOPIC = "contract-events";

    public ContractService(ContractRepository contractRepository,
            CustomerClient customerClient,
            AssetClient assetClient,
            NotificationClient notificationClient,
            KafkaTemplate<String, ContractEvent> kafkaTemplate) {
        this.contractRepository = contractRepository;
        this.customerClient = customerClient;
        this.assetClient = assetClient;
        this.notificationClient = notificationClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public List<Contract> findAll() {
        return contractRepository.findAll();
    }

    public Contract findById(UUID id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contract", "id", id));
    }

    public List<Contract> findByCustomerId(UUID customerId) {
        return contractRepository.findByCustomerId(customerId);
    }

    @Transactional
    public Contract create(Contract contract) {
        // Validate customer exists via Feign client
        try {
            customerClient.getCustomer(contract.getCustomerId());
            log.info("Customer {} validated via CustomerClient", contract.getCustomerId());
        } catch (Exception e) {
            throw new BadRequestException("Customer validation failed: " + e.getMessage());
        }

        // Validate asset exists via Feign client
        try {
            assetClient.getAsset(contract.getAssetId());
            log.info("Asset {} validated via AssetClient", contract.getAssetId());
        } catch (Exception e) {
            throw new BadRequestException("Asset validation failed: " + e.getMessage());
        }

        contract.setStatus(Contract.ContractStatus.DRAFT);
        Contract saved = contractRepository.save(contract);
        log.info("Created contract: {}", saved.getId());
        kafkaTemplate.send(CONTRACT_TOPIC, saved.getId().toString(), ContractEvent.created(saved.getId()));

        // Send notification
        sendContractNotification(saved, "CONTRACT_CREATED", "Contract created in draft status");

        return saved;
    }

    @Transactional
    public Contract submitForApproval(UUID id) {
        Contract contract = findById(id);
        if (contract.getStatus() != Contract.ContractStatus.DRAFT) {
            throw new BadRequestException("Contract must be in DRAFT status to submit for approval");
        }
        contract.setStatus(Contract.ContractStatus.PENDING_APPROVAL);
        log.info("Contract {} submitted for approval", id);
        Contract saved = contractRepository.save(contract);
        kafkaTemplate.send(CONTRACT_TOPIC, id.toString(),
                ContractEvent.statusChanged(id, "DRAFT", "PENDING_APPROVAL", "Submitted for approval"));

        sendContractNotification(saved, "CONTRACT_SUBMITTED", "Contract submitted for approval");

        return saved;
    }

    @Transactional
    public Contract approve(UUID id) {
        Contract contract = findById(id);
        if (contract.getStatus() != Contract.ContractStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Contract must be in PENDING_APPROVAL status to approve");
        }
        contract.setStatus(Contract.ContractStatus.APPROVED);
        log.info("Contract {} approved", id);
        Contract saved = contractRepository.save(contract);
        kafkaTemplate.send(CONTRACT_TOPIC, id.toString(),
                ContractEvent.statusChanged(id, "PENDING_APPROVAL", "APPROVED", "Contract approved"));

        sendContractNotification(saved, "CONTRACT_APPROVED", "Contract has been approved");

        return saved;
    }

    @Transactional
    public Contract activate(UUID id) {
        Contract contract = findById(id);
        if (contract.getStatus() != Contract.ContractStatus.APPROVED) {
            throw new BadRequestException("Contract must be in APPROVED status to activate");
        }

        // Reserve asset when contract is activated
        try {
            assetClient.updateStatus(contract.getAssetId(), "RESERVED");
            log.info("Asset {} reserved via AssetClient", contract.getAssetId());
        } catch (Exception e) {
            throw new BadRequestException("Failed to reserve asset: " + e.getMessage());
        }

        contract.setStatus(Contract.ContractStatus.ACTIVE);
        log.info("Contract {} activated", id);
        Contract saved = contractRepository.save(contract);
        kafkaTemplate.send(CONTRACT_TOPIC, id.toString(),
                ContractEvent.statusChanged(id, "APPROVED", "ACTIVE", "Contract activated"));

        sendContractNotification(saved, "CONTRACT_ACTIVATED", "Contract is now active");

        return saved;
    }

    @Transactional
    public Contract cancel(UUID id) {
        Contract contract = findById(id);

        // Release asset if contract was active
        if (contract.getStatus() == Contract.ContractStatus.ACTIVE) {
            try {
                assetClient.updateStatus(contract.getAssetId(), "AVAILABLE");
                log.info("Asset {} released via AssetClient", contract.getAssetId());
            } catch (Exception e) {
                log.warn("Failed to release asset: {}", e.getMessage());
            }
        }

        String previousStatus = contract.getStatus().name();
        contract.setStatus(Contract.ContractStatus.CANCELLED);
        log.info("Contract {} cancelled", id);
        Contract saved = contractRepository.save(contract);
        kafkaTemplate.send(CONTRACT_TOPIC, id.toString(),
                ContractEvent.statusChanged(id, previousStatus, "CANCELLED", "Contract cancelled"));

        sendContractNotification(saved, "CONTRACT_CANCELLED", "Contract has been cancelled");

        return saved;
    }

    private void sendContractNotification(Contract contract, String type, String message) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("recipientId", contract.getCustomerId());
            notification.put("type", type);
            notification.put("title", "Contract Update");
            notification.put("message", message);
            notification.put("metadata", Map.of("contractId", contract.getId().toString()));

            notificationClient.sendNotification(notification);
            log.info("Sent {} notification for contract {}", type, contract.getId());
        } catch (Exception e) {
            log.warn("Failed to send notification for contract {}: {}", contract.getId(), e.getMessage());
        }
    }
}
