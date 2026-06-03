package com.smartasset.asset.service;

import com.smartasset.asset.domain.Asset;
import com.smartasset.common.events.AssetEvent;
import com.smartasset.asset.repository.AssetRepository;
import com.smartasset.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AssetService {

    private static final Logger log = LoggerFactory.getLogger(AssetService.class);
    private static final String ASSET_TOPIC = "asset-events";

    private final AssetRepository assetRepository;
    private final KafkaTemplate<String, AssetEvent> kafkaTemplate;

    public AssetService(AssetRepository assetRepository, KafkaTemplate<String, AssetEvent> kafkaTemplate) {
        this.assetRepository = assetRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public List<Asset> findAll() {
        return assetRepository.findAll();
    }

    public Asset findById(UUID id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asset", "id", id));
    }

    public List<Asset> findByStatus(Asset.AssetStatus status) {
        return assetRepository.findByStatus(status);
    }

    @Transactional
    public Asset create(Asset asset) {
        Asset saved = assetRepository.save(asset);
        log.info("Created asset: {}", saved.getId());
        kafkaTemplate.send(ASSET_TOPIC, saved.getId().toString(), AssetEvent.created(saved.getId()));
        return saved;
    }

    @Transactional
    public Asset update(UUID id, Asset assetDetails) {
        Asset asset = findById(id);
        asset.setName(assetDetails.getName());
        asset.setCategory(assetDetails.getCategory());
        asset.setValue(assetDetails.getValue());
        asset.setDescription(assetDetails.getDescription());
        return assetRepository.save(asset);
    }

    @Transactional
    public Asset updateStatus(UUID id, Asset.AssetStatus newStatus) {
        Asset asset = findById(id);
        String previousStatus = asset.getStatus().name();
        asset.setStatus(newStatus);
        Asset saved = assetRepository.save(asset);
        log.info("Asset {} status changed from {} to {}", id, previousStatus, newStatus);
        kafkaTemplate.send(ASSET_TOPIC, id.toString(), AssetEvent.statusChanged(id, previousStatus, newStatus.name()));
        return saved;
    }

    @Transactional
    public void delete(UUID id) {
        Asset asset = findById(id);
        assetRepository.delete(asset);
        log.info("Deleted asset: {}", id);
    }
}
