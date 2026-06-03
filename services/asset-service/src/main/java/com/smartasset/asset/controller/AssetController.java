package com.smartasset.asset.controller;

import com.smartasset.asset.domain.Asset;
import com.smartasset.asset.service.AssetService;
import com.smartasset.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Asset>>> getAllAssets() {
        return ResponseEntity.ok(ApiResponse.success(assetService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Asset>> getAssetById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(assetService.findById(id)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Asset>>> getAssetsByStatus(@PathVariable Asset.AssetStatus status) {
        return ResponseEntity.ok(ApiResponse.success(assetService.findByStatus(status)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Asset>> createAsset(@RequestBody Asset asset) {
        Asset created = assetService.create(asset);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Asset created", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Asset>> updateAsset(@PathVariable UUID id, @RequestBody Asset asset) {
        return ResponseEntity.ok(ApiResponse.success("Asset updated", assetService.update(id, asset)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Asset>> updateAssetStatus(@PathVariable UUID id, @RequestParam Asset.AssetStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Asset status updated", assetService.updateStatus(id, status)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAsset(@PathVariable UUID id) {
        assetService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Asset deleted", null));
    }
}
