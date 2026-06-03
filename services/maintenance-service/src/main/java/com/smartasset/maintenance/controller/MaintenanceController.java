package com.smartasset.maintenance.controller;

import com.smartasset.maintenance.domain.MaintenanceRecord;
import com.smartasset.maintenance.service.MaintenanceService;
import com.smartasset.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MaintenanceRecord>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(maintenanceService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MaintenanceRecord>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(maintenanceService.findById(id)));
    }

    @GetMapping("/asset/{assetId}")
    public ResponseEntity<ApiResponse<List<MaintenanceRecord>>> getByAsset(@PathVariable UUID assetId) {
        return ResponseEntity.ok(ApiResponse.success(maintenanceService.findByAssetId(assetId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MaintenanceRecord>> schedule(@RequestBody MaintenanceRecord record) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Maintenance scheduled", maintenanceService.schedule(record)));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<MaintenanceRecord>> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Maintenance completed", maintenanceService.complete(id)));
    }
}
