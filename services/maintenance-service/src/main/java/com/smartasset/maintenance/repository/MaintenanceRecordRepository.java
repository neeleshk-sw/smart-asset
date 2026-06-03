package com.smartasset.maintenance.repository;

import com.smartasset.maintenance.domain.MaintenanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, UUID> {
    List<MaintenanceRecord> findByAssetId(UUID assetId);
    List<MaintenanceRecord> findByStatus(MaintenanceRecord.MaintenanceStatus status);
}
