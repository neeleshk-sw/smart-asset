package com.smartasset.audit.messaging;

import com.smartasset.audit.domain.AuditLog;
import com.smartasset.audit.service.AuditService;
import com.smartasset.common.events.AssetEvent;
import com.smartasset.common.events.CustomerEvent;
import com.smartasset.common.events.ContractEvent;
import com.smartasset.common.events.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class KafkaConsumerListener {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerListener.class);
    private final AuditService auditService;

    public KafkaConsumerListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @KafkaListener(topics = "asset-events", groupId = "audit-service-group")
    public void consumeAssetEvent(AssetEvent event) {
        log.info("Consumed AssetEvent: {} for asset {}", event.getEventType(), event.getAssetId());
        AuditLog auditLog = new AuditLog();
        auditLog.setEntityType("ASSET");
        auditLog.setEntityId(event.getAssetId());
        auditLog.setAction(event.getEventType());
        auditLog.setDetails(
                String.format("Status changed from %s to %s", event.getPreviousStatus(), event.getNewStatus()));
        auditService.log(auditLog);
    }

    @KafkaListener(topics = "customer-events", groupId = "audit-service-group")
    public void consumeCustomerEvent(CustomerEvent event) {
        log.info("Consumed CustomerEvent: {} for customer {}", event.getEventType(), event.getCustomerId());
        AuditLog auditLog = new AuditLog();
        auditLog.setEntityType("CUSTOMER");
        auditLog.setEntityId(event.getCustomerId());
        auditLog.setAction(event.getEventType());
        auditLog.setDetails(String.format("KYC status: %s", event.getKycStatus()));
        auditService.log(auditLog);
    }

    @KafkaListener(topics = "contract-events", groupId = "audit-service-group")
    public void consumeContractEvent(ContractEvent event) {
        log.info("Consumed ContractEvent: {} for contract {}", event.getEventType(), event.getContractId());
        AuditLog auditLog = new AuditLog();
        auditLog.setEntityType("CONTRACT");
        auditLog.setEntityId(event.getContractId());
        auditLog.setAction(event.getEventType());
        auditLog.setDetails(event.getDetails());
        auditService.log(auditLog);
    }

    @KafkaListener(topics = "payment-events", groupId = "audit-service-group")
    public void consumePaymentEvent(PaymentEvent event) {
        log.info("Consumed PaymentEvent: {} for payment {}", event.getEventType(), event.getPaymentId());
        AuditLog auditLog = new AuditLog();
        auditLog.setEntityType("PAYMENT");
        auditLog.setEntityId(event.getPaymentId());
        auditLog.setAction(event.getEventType());
        auditLog.setDetails(String.format("Status: %s, Amount: %s", event.getStatus(), event.getAmount()));
        auditService.log(auditLog);
    }
}
