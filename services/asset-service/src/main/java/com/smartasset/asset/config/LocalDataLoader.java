package com.smartasset.asset.config;

import com.smartasset.asset.domain.Asset;
import com.smartasset.asset.repository.AssetRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Configuration
@Profile("local")
public class LocalDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LocalDataLoader.class);

    private final AssetRepository assetRepository;

    public LocalDataLoader(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Override
    public void run(String... args) {
        if (assetRepository.count() > 0) {
            log.info("Asset database already seeded. Skipping local data loading.");
            return;
        }

        log.info("Seeding local asset data...");

        Asset asset1 = new Asset();
        asset1.setId(UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"));
        asset1.setName("ThinkPad X1 Carbon");
        asset1.setCategory("Laptop");
        asset1.setStatus(Asset.AssetStatus.AVAILABLE);
        asset1.setValue(new BigDecimal("1500.00"));
        asset1.setDescription("Enterprise laptop with 32GB RAM, 1TB SSD");

        Asset asset2 = new Asset();
        asset2.setId(UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"));
        asset2.setName("MacBook Pro 16\"");
        asset2.setCategory("Laptop");
        asset2.setStatus(Asset.AssetStatus.AVAILABLE);
        asset2.setValue(new BigDecimal("2500.00"));
        asset2.setDescription("M2 Max chip, 64GB RAM, 2TB SSD");

        Asset asset3 = new Asset();
        asset3.setId(UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc"));
        asset3.setName("Tesla Model 3");
        asset3.setCategory("Vehicle");
        asset3.setStatus(Asset.AssetStatus.AVAILABLE);
        asset3.setValue(new BigDecimal("45000.00"));
        asset3.setDescription("Electric vehicle, AWD Long Range");

        Asset asset4 = new Asset();
        asset4.setId(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"));
        asset4.setName("Dell U2723QE");
        asset4.setCategory("Monitor");
        asset4.setStatus(Asset.AssetStatus.AVAILABLE);
        asset4.setValue(new BigDecimal("600.00"));
        asset4.setDescription("27-inch 4K USB-C Hub Monitor");

        assetRepository.saveAll(List.of(asset1, asset2, asset3, asset4));

        log.info("Local asset data seeded successfully.");
    }
}
