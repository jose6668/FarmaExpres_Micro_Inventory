package co.edu.corhuila.inventory_service.Dto;

import java.time.LocalDate;

public class FefoSnapshotItemResponse {
    private Long productId;
    private String productCode;
    private String productName;
    private Integer operationalStock;
    private String nextBatchCode;
    private LocalDate nextExpirationDate;
    private Integer activeBatchesCount;

    public FefoSnapshotItemResponse(
            Long productId,
            String productCode,
            String productName,
            Integer operationalStock,
            String nextBatchCode,
            LocalDate nextExpirationDate,
            Integer activeBatchesCount
    ) {
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.operationalStock = operationalStock;
        this.nextBatchCode = nextBatchCode;
        this.nextExpirationDate = nextExpirationDate;
        this.activeBatchesCount = activeBatchesCount;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductCode() {
        return productCode;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getOperationalStock() {
        return operationalStock;
    }

    public String getNextBatchCode() {
        return nextBatchCode;
    }

    public LocalDate getNextExpirationDate() {
        return nextExpirationDate;
    }

    public Integer getActiveBatchesCount() {
        return activeBatchesCount;
    }
}
