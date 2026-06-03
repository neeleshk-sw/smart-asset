package com.smartasset.contract.repository;

import com.smartasset.contract.domain.Contract;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ContractRepositoryIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("smartasset")
            .withUsername("user")
            .withPassword("password")
            .withInitScript("init-db.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private ContractRepository contractRepository;

    @Test
    void saveAndFindById_ShouldWork() {
        Contract contract = new Contract();
        contract.setCustomerId(UUID.randomUUID());
        contract.setAssetId(UUID.randomUUID());
        contract.setTotalValue(new BigDecimal("12000.00"));
        contract.setStatus(Contract.ContractStatus.DRAFT);

        Contract savedContract = contractRepository.save(contract);
        assertThat(savedContract.getId()).isNotNull();

        Optional<Contract> foundContract = contractRepository.findById(savedContract.getId());
        assertThat(foundContract).isPresent();
        assertThat(foundContract.get().getTotalValue()).isEqualByComparingTo("12000.00");
    }

    @Test
    void findByCustomerId_ShouldReturnContracts() {
        UUID customerId = UUID.randomUUID();
        Contract contract1 = createContract(customerId, UUID.randomUUID());
        Contract contract2 = createContract(customerId, UUID.randomUUID());
        contractRepository.saveAll(List.of(contract1, contract2));

        List<Contract> contracts = contractRepository.findByCustomerId(customerId);
        assertThat(contracts).hasSize(2);
    }

    private Contract createContract(UUID customerId, UUID assetId) {
        Contract contract = new Contract();
        contract.setCustomerId(customerId);
        contract.setAssetId(assetId);
        contract.setTotalValue(new BigDecimal("5000.00"));
        contract.setStatus(Contract.ContractStatus.DRAFT);
        return contract;
    }
}
