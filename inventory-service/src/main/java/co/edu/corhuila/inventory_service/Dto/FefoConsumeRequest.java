package co.edu.corhuila.inventory_service.Dto;

public class FefoConsumeRequest {
    private Long productId;
    private Integer quantity;
    private String reason;
    private String detail;

    public Long getProductId() {
        return productId;
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

