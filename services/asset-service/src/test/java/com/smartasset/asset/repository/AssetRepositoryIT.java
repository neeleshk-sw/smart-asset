package com.smartasset.asset.repository;

import com.smartasset.asset.domain.Asset;
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
public class AssetRepositoryIT {

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
    private AssetRepository assetRepository;

    @Test
    void saveAndFindById_ShouldWork() {
        Asset asset = new Asset();
        asset.setName("Test Asset");
        asset.setCategory("Electronics");
        asset.setValue(new BigDecimal("1000.00"));
        asset.setStatus(Asset.AssetStatus.AVAILABLE);

        Asset savedAsset = assetRepository.save(asset);
        assertThat(savedAsset.getId()).isNotNull();

        Optional<Asset> foundAsset = assetRepository.findById(savedAsset.getId());
        assertThat(foundAsset).isPresent();
        assertThat(foundAsset.get().getName()).isEqualTo("Test Asset");
    }

    @Test
    void findByStatus_ShouldReturnMatchingAssets() {
        Asset asset1 = createAsset("Asset 1", Asset.AssetStatus.AVAILABLE);
        Asset asset2 = createAsset("Asset 2", Asset.AssetStatus.LEASED);
        assetRepository.saveAll(List.of(asset1, asset2));

        List<Asset> availableAssets = assetRepository.findByStatus(Asset.AssetStatus.AVAILABLE);
        assertThat(availableAssets).hasSize(1);
        assertThat(availableAssets.get(0).getName()).isEqualTo("Asset 1");
    }

    @Test
    void findByCategory_ShouldReturnMatchingAssets() {
        Asset asset1 = createAsset("Asset 1", Asset.AssetStatus.AVAILABLE);
        asset1.setCategory("Vehicles");
        Asset asset2 = createAsset("Asset 2", Asset.AssetStatus.AVAILABLE);
        asset2.setCategory("Real Estate");
        assetRepository.saveAll(List.of(asset1, asset2));

        List<Asset> vehicles = assetRepository.findByCategory("Vehicles");
        assertThat(vehicles).hasSize(1);
        assertThat(vehicles.get(0).getName()).isEqualTo("Asset 1");
    }

    private Asset createAsset(String name, Asset.AssetStatus status) {
        Asset asset = new Asset();
        asset.setName(name);
        asset.setCategory("General");
        asset.setValue(new BigDecimal("500.00"));
        asset.setStatus(status);
        return asset;
    }
}
