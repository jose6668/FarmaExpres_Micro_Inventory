package co.edu.corhuila.inventory_service.Dto;

import java.util.List;

public class InventoryEntryResponse {
    private String type;
    private Long productId;
    private Integer requestedQuantity;
    private String reason;
    private String detail;
    private List<FefoConsumptionItemResponse> allocations;

    public InventoryEntryResponse(
            String type,
            Long productId,
            Integer requestedQuantity,
            String reason,
            String detail,
            List<FefoConsumptionItemResponse> allocations
    ) {
        this.type = type;
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.reason = reason;
        this.detail = detail;
        this.allocations = allocations;
    }

    public String getType() {
        return type;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public String getReason() {
        return reason;
    }

    public String getDetail() {
        return detail;
    }

    public List<FefoConsumptionItemResponse> getAllocations() {
        return allocations;
    }
}
