package com.smartasset.payment.config;

import com.smartasset.payment.domain.Payment;
import com.smartasset.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Configuration
@Profile("local")
public class LocalDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LocalDataLoader.class);

    private final PaymentRepository paymentRepository;

    public LocalDataLoader(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public void run(String... args) {
        if (paymentRepository.count() > 0) {
            log.info("Payment database already seeded. Skipping local data loading.");
            return;
        }

        log.info("Seeding local payment data...");

        // Referencing UUIDs from Contract loader
        UUID contract1Id = UUID.fromString("c1111111-1111-1111-1111-111111111111");

        Payment payment1 = new Payment();
        payment1.setContractId(contract1Id);
        payment1.setAmount(new BigDecimal("1500.00"));
        payment1.setDueDate(LocalDate.now().minusMonths(2));
        payment1.setPaidDate(LocalDate.now().minusMonths(2).plusDays(5));
        payment1.setStatus(Payment.PaymentStatus.PAID);
        payment1.setInstallmentNumber(1);

        Payment payment2 = new Payment();
        payment2.setContractId(contract1Id);
        payment2.setAmount(new BigDecimal("1500.00"));
        payment2.setDueDate(LocalDate.now().minusMonths(1));
        payment2.setPaidDate(LocalDate.now().minusMonths(1).plusDays(3));
        payment2.setStatus(Payment.PaymentStatus.PAID);
        payment2.setInstallmentNumber(2);

        Payment payment3 = new Payment();
        payment3.setContractId(contract1Id);
        payment3.setAmount(new BigDecimal("1500.00"));
        payment3.setDueDate(LocalDate.now());
        payment3.setStatus(Payment.PaymentStatus.PENDING);
        payment3.setInstallmentNumber(3);

        paymentRepository.saveAll(List.of(payment1, payment2, payment3));

        log.info("Local payment data seeded successfully.");
    }
}
