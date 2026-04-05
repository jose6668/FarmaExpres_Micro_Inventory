package co.edu.corhuila.inventory_service.Dto;

import co.edu.corhuila.inventory_service.Entity.Motion;

import java.time.Instant;
import java.time.LocalDate;

public class MovementBatchReportItemResponse {
    private Long movementId;
    private String movementType;
    private Instant movementDateTime;
    private Integer quantity;
    private Long productId;
    private String productCode;
    private String productName;
    private Long batchId;
    private String batchCode;
    private LocalDate expirationDate;
    private Integer availableStock;
    private String reason;

    public MovementBatchReportItemResponse(Motion motion) {
        this.movementId = motion.getId();
        this.movementType = motion.getType().name();
        this.movementDateTime = motion.getDateTime();
        this.quantity = motion.getAmount();
        this.productId = motion.getProduct().getId();
        this.productCode = motion.getProduct().getCode();
        this.productName = motion.getProduct().getName();
        this.batchId = motion.getBatch().getId();
        this.batchCode = motion.getBatch().getBatchCode();
        this.expirationDate = motion.getBatch().getExpirationDate();
        this.availableStock = motion.getBatch().getAvailableStock();
        this.reason = motion.getReason();
    }

    public Long getMovementId() {
        return movementId;
    }

    public String getMovementType() {
        return movementType;
    }

    public Instant getMovementDateTime() {
        return movementDateTime;
    }

    public Integer getQuantity() {
        return quantity;
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

    public String getReason() {
        return reason;
    }
}

