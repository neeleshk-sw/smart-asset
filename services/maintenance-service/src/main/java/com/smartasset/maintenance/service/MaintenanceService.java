package com.smartasset.maintenance.service;

import com.smartasset.maintenance.domain.MaintenanceRecord;
import com.smartasset.maintenance.repository.MaintenanceRecordRepository;
import com.smartasset.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class MaintenanceService {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceService.class);
    private final MaintenanceRecordRepository repository;

    public MaintenanceService(MaintenanceRecordRepository repository) {
        this.repository = repository;
    }

    public List<MaintenanceRecord> findAll() { return repository.findAll(); }

    public MaintenanceRecord findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("MaintenanceRecord", "id", id));
    }

    public List<MaintenanceRecord> findByAssetId(UUID assetId) { return repository.findByAssetId(assetId); }

    public MaintenanceRecord schedule(MaintenanceRecord record) {
        record.setStatus(MaintenanceRecord.MaintenanceStatus.SCHEDULED);
        MaintenanceRecord saved = repository.save(record);
        log.info("Scheduled maintenance {} for asset {}", saved.getId(), saved.getAssetId());
        return saved;
    }

    public MaintenanceRecord complete(UUID id) {
        MaintenanceRecord record = findById(id);
        record.setStatus(MaintenanceRecord.MaintenanceStatus.COMPLETED);
        record.setCompletedDate(LocalDate.now());
        return repository.save(record);
    }
}
