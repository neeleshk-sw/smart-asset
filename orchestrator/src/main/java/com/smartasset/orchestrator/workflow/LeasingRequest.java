package com.smartasset.orchestrator.workflow;

import java.math.BigDecimal;
import java.util.UUID;

public class LeasingRequest {
    private UUID customerId;
    private UUID assetId;
    private BigDecimal totalValue;
    private int termMonths;

    public LeasingRequest() {}

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }

    public UUID getAssetId() { return assetId; }
    public void setAssetId(UUID assetId) { this.assetId = assetId; }

    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }

    public int getTermMonths() { return termMonths; }
    public void setTermMonths(int termMonths) { this.termMonths = termMonths; }
}
