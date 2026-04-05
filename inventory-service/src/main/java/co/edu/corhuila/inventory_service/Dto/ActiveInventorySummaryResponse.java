package co.edu.corhuila.inventory_service.Dto;

import java.math.BigDecimal;

public class ActiveInventorySummaryResponse {

    private final Integer totalStock;
    private final BigDecimal totalInventoryValue;

    public ActiveInventorySummaryResponse(Integer totalStock, BigDecimal totalInventoryValue) {
        this.totalStock = totalStock;
        this.totalInventoryValue = totalInventoryValue;
    }

    public Integer getTotalStock() {
        return totalStock;
    }

    public BigDecimal getTotalInventoryValue() {
        return totalInventoryValue;
    }
}
