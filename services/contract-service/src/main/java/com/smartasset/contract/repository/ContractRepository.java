package com.smartasset.contract.repository;

import com.smartasset.contract.domain.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContractRepository extends JpaRepository<Contract, UUID> {
    List<Contract> findByCustomerId(UUID customerId);
    List<Contract> findByAssetId(UUID assetId);
    List<Contract> findByStatus(Contract.ContractStatus status);
}
