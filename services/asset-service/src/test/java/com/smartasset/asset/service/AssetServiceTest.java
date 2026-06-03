package com.smartasset.asset.service;

import com.smartasset.asset.domain.Asset;
import com.smartasset.asset.repository.AssetRepository;
import com.smartasset.common.events.AssetEvent;
import com.smartasset.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private KafkaTemplate<String, AssetEvent> kafkaTemplate;

    private AssetService assetService;

    @BeforeEach
    void setUp() {
        assetService = new AssetService(assetRepository, kafkaTemplate);
    }

    @Test
    void findAll_ShouldReturnList() {
        Asset asset1 = new Asset();
        Asset asset2 = new Asset();
        when(assetRepository.findAll()).thenReturn(Arrays.asList(asset1, asset2));

        List<Asset> result = assetService.findAll();

        assertThat(result).hasSize(2);
        verify(assetRepository, times(1)).findAll();
    }

    @Test
    void findById_WhenAssetExists_ShouldReturnAsset() {
        UUID id = UUID.randomUUID();
        Asset asset = new Asset();
        asset.setId(id);
        when(assetRepository.findById(id)).thenReturn(Optional.of(asset));

        Asset result = assetService.findById(id);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void findById_WhenAssetDoesNotExist_ShouldThrowException() {
        UUID id = UUID.randomUUID();
        when(assetRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> assetService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_ShouldSaveAssetAndSendKafkaEvent() {
        Asset asset = new Asset();
        UUID id = UUID.randomUUID();
        asset.setId(id);
        asset.setName("Test Asset");
        asset.setStatus(Asset.AssetStatus.AVAILABLE);

        when(assetRepository.save(any(Asset.class))).thenReturn(asset);

        Asset result = assetService.create(asset);

        assertThat(result).isEqualTo(asset);
        verify(assetRepository, times(1)).save(asset);

        ArgumentCaptor<AssetEvent> eventCaptor = ArgumentCaptor.forClass(AssetEvent.class);
        verify(kafkaTemplate).send(eq("asset-events"), eq(id.toString()), eventCaptor.capture());

        AssetEvent event = eventCaptor.getValue();
        assertThat(event.getAssetId()).isEqualTo(id);
        assertThat(event.getEventType()).isEqualTo("ASSET_CREATED");
        assertThat(event.getNewStatus()).isEqualTo("AVAILABLE");
    }

    @Test
    void updateStatus_ShouldChangeStatusAndSendKafkaEvent() {
        UUID id = UUID.randomUUID();
        Asset asset = new Asset();
        asset.setId(id);
        asset.setStatus(Asset.AssetStatus.AVAILABLE);

        when(assetRepository.findById(id)).thenReturn(Optional.of(asset));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Asset result = assetService.updateStatus(id, Asset.AssetStatus.LEASED);

        assertThat(result.getStatus()).isEqualTo(Asset.AssetStatus.LEASED);
        verify(assetRepository).save(asset);

        ArgumentCaptor<AssetEvent> eventCaptor = ArgumentCaptor.forClass(AssetEvent.class);
        verify(kafkaTemplate).send(eq("asset-events"), eq(id.toString()), eventCaptor.capture());

        AssetEvent event = eventCaptor.getValue();
        assertThat(event.getAssetId()).isEqualTo(id);
        assertThat(event.getEventType()).isEqualTo("ASSET_STATUS_CHANGED");
        assertThat(event.getPreviousStatus()).isEqualTo("AVAILABLE");
        assertThat(event.getNewStatus()).isEqualTo("LEASED");
    }

    @Test
    void delete_WhenAssetExists_ShouldDelete() {
        UUID id = UUID.randomUUID();
        Asset asset = new Asset();
        asset.setId(id);
        when(assetRepository.findById(id)).thenReturn(Optional.of(asset));

        assetService.delete(id);

        verify(assetRepository).delete(asset);
    }
}
