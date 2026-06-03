package com.smartasset.asset.repository;

import com.smartasset.asset.domain.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssetRepository extends JpaRepository<Asset, UUID> {
    List<Asset> findByStatus(Asset.AssetStatus status);
    List<Asset> findByCategory(String category);
}
