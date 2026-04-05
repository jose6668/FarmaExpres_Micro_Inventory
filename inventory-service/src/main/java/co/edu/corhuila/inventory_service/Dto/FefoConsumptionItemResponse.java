package co.edu.corhuila.inventory_service.Dto;

import java.time.LocalDate;

public class FefoConsumptionItemResponse {
    private Long batchId;
    private String batchCode;
    private LocalDate expirationDate;
    private Integer quantity;

    public FefoConsumptionItemResponse(Long batchId, String batchCode, LocalDate expirationDate, Integer quantity) {
        this.batchId = batchId;
        this.batchCode = batchCode;
        this.expirationDate = expirationDate;
        this.quantity = quantity;
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

    public Integer getQuantity() {
        return quantity;
    }

    public Integer getConsumedQuantity() {
        return quantity;
    }
}

