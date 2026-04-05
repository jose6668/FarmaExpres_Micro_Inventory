package co.edu.corhuila.inventory_service.Dto;

public class MovementRequest {
    private String type;
    private Long productId;
    private Long batchId;
    private Integer quantity;
    private String reason;
    private String detail;

    public String getType() {
        return type;
    }

    public Long getProductId() {
        return productId;
    }

    public Long getBatchId() {
        return batchId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getReason() {
        return reason;
    }

    public String getDetail() {
        return detail;
    }
}

