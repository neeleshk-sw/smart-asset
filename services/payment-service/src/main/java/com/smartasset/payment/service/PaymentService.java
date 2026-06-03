package com.smartasset.payment.service;

import com.smartasset.payment.domain.Payment;
import com.smartasset.payment.repository.PaymentRepository;
import com.smartasset.clients.ContractClient;
import com.smartasset.common.events.PaymentEvent;
import com.smartasset.common.exception.BadRequestException;
import com.smartasset.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final ContractClient contractClient;
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    private static final String PAYMENT_TOPIC = "payment-events";

    public PaymentService(PaymentRepository paymentRepository, ContractClient contractClient,
            KafkaTemplate<String, PaymentEvent> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.contractClient = contractClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }

    public Payment findById(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
    }

    public List<Payment> findByContractId(UUID contractId) {
        return paymentRepository.findByContractId(contractId);
    }

    @Transactional
    public List<Payment> generateSchedule(UUID contractId, BigDecimal totalAmount, int termMonths,
            LocalDate startDate) {
        // Validate contract exists via Feign client
        try {
            contractClient.getContract(contractId);
            log.info("Contract {} validated via ContractClient", contractId);
        } catch (Exception e) {
            throw new BadRequestException("Contract validation failed: " + e.getMessage());
        }

        List<Payment> schedule = new ArrayList<>();
        BigDecimal monthlyPayment = totalAmount.divide(BigDecimal.valueOf(termMonths), 2, RoundingMode.HALF_UP);

        for (int i = 1; i <= termMonths; i++) {
            Payment payment = new Payment();
            payment.setContractId(contractId);
            payment.setAmount(monthlyPayment);
            payment.setDueDate(startDate.plusMonths(i));
            payment.setInstallmentNumber(i);
            payment.setStatus(Payment.PaymentStatus.PENDING);
            schedule.add(payment);
        }

        List<Payment> saved = paymentRepository.saveAll(schedule);
        log.info("Generated {} payment schedule entries for contract {}", termMonths, contractId);
        saved.forEach(p -> kafkaTemplate.send(PAYMENT_TOPIC, p.getId().toString(),
                PaymentEvent.created(p.getId(), contractId, p.getAmount())));
        return saved;
    }

    @Transactional
    public Payment markAsPaid(UUID id) {
        Payment payment = findById(id);
        payment.setStatus(Payment.PaymentStatus.PAID);
        payment.setPaidDate(LocalDate.now());
        log.info("Payment {} marked as paid", id);
        Payment saved = paymentRepository.save(payment);
        kafkaTemplate.send(PAYMENT_TOPIC, id.toString(), PaymentEvent.statusChanged(id, saved.getContractId(), "PAID"));
        return saved;
    }

    @Transactional
    public Payment markAsOverdue(UUID id) {
        Payment payment = findById(id);
        payment.setStatus(Payment.PaymentStatus.OVERDUE);
        Payment saved = paymentRepository.save(payment);
        kafkaTemplate.send(PAYMENT_TOPIC, id.toString(),
                PaymentEvent.statusChanged(id, saved.getContractId(), "OVERDUE"));
        return saved;
    }
}
