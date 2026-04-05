package co.edu.corhuila.inventory_service.Dto;

import co.edu.corhuila.inventory_service.Entity.Batch;

import java.time.LocalDate;

public class BatchResponse {
    private Long id;
    private Long batchId;
    private Long productId;
    private String batchCode;
    private LocalDate expirationDate;
    private Integer initialStock;
    private Integer availableStock;
    private String status;

    public BatchResponse(Batch batch) {
        this.id = batch.getId();
        this.batchId = batch.getId();
        this.productId = batch.getProduct().getId();
        this.batchCode = batch.getBatchCode();
        this.expirationDate = batch.getExpirationDate();
        this.initialStock = batch.getInitialStock();
        this.availableStock = batch.getAvailableStock();
        this.status = batch.getStatus().name();
    }

    public Long getId() {
        return id;
    }

    public Long getBatchId() {
        return batchId;
    }

    public Long getProductId() {
        return productId;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public Integer getInitialStock() {
        return initialStock;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public String getStatus() {
        return status;
    }
}
