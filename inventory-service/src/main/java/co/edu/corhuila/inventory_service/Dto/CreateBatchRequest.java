package co.edu.corhuila.inventory_service.Dto;

import java.time.LocalDate;

public class CreateBatchRequest {
    private String batchCode;
    private LocalDate expirationDate;
    private Integer initialStock;

    public String getBatchCode() {
        return batchCode;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public Integer getInitialStock() {
        return initialStock;
    }
}

