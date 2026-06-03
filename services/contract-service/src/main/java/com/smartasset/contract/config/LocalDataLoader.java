package com.smartasset.contract.config;

import com.smartasset.contract.domain.Contract;
import com.smartasset.contract.repository.ContractRepository;
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

    private final ContractRepository contractRepository;

    public LocalDataLoader(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    @Override
    public void run(String... args) {
        if (contractRepository.count() > 0) {
            log.info("Contract database already seeded. Skipping local data loading.");
            return;
        }

        log.info("Seeding local contract data...");

        // Referencing UUIDs from Customer and Asset loaders
        UUID customer1Id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID asset1Id = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        UUID asset2Id = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

        Contract contract1 = new Contract();
        contract1.setId(UUID.fromString("c1111111-1111-1111-1111-111111111111"));
        contract1.setCustomerId(customer1Id);
        contract1.setAssetId(asset1Id);
        contract1.setStatus(Contract.ContractStatus.ACTIVE);
        contract1.setTotalValue(new BigDecimal("18000.00"));
        contract1.setMonthlyPayment(new BigDecimal("1500.00"));
        contract1.setTermMonths(12);
        contract1.setStartDate(LocalDate.now().minusMonths(2));
        contract1.setEndDate(LocalDate.now().plusMonths(10));

        Contract contract2 = new Contract();
        contract2.setId(UUID.fromString("c2222222-2222-2222-2222-222222222222"));
        contract2.setCustomerId(customer1Id);
        contract2.setAssetId(asset2Id);
        contract2.setStatus(Contract.ContractStatus.APPROVED);
        contract2.setTotalValue(new BigDecimal("60000.00"));
        contract2.setMonthlyPayment(new BigDecimal("2500.00"));
        contract2.setTermMonths(24);
        contract2.setStartDate(LocalDate.now().plusDays(1));
        contract2.setEndDate(LocalDate.now().plusMonths(24));

        contractRepository.saveAll(List.of(contract1, contract2));

        log.info("Local contract data seeded successfully.");
    }
}
