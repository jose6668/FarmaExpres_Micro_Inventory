package co.edu.corhuila.inventory_service.Dto;

import co.edu.corhuila.inventory_service.Entity.Batch;

import java.time.LocalDate;

public class InventoryBatchReportItemResponse {
    private Long productId;
    private String productCode;
    private String productName;
    private Long batchId;
    private String batchCode;
    private LocalDate expirationDate;
    private Integer availableStock;
    private String status;

    public InventoryBatchReportItemResponse(Batch batch) {
        this.productId = batch.getProduct().getId();
        this.productCode = batch.getProduct().getCode();
        this.productName = batch.getProduct().getName();
        this.batchId = batch.getId();
        this.batchCode = batch.getBatchCode();
        this.expirationDate = batch.getExpirationDate();
        this.availableStock = batch.getAvailableStock();
        this.status = batch.getStatus().name();
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

    public Long getBatchId() {
        return batchId;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public String getStatus() {
        return status;
    }
}

