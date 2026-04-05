package co.edu.corhuila.inventory_service.Dto;

import java.math.BigDecimal;

public class ActiveInventoryTableItemResponse {

    private final String code;
    private final String name;
    private final Integer stock;
    private final BigDecimal unitPrice;
    private final BigDecimal totalValue;

    public ActiveInventoryTableItemResponse(
            String code,
            String name,
            Integer stock,
            BigDecimal unitPrice,
            BigDecimal totalValue
    ) {
        this.code = code;
        this.name = name;
        this.stock = stock;
        this.unitPrice = unitPrice;
        this.totalValue = totalValue;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Integer getStock() {
        return stock;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getTotalValue() {
        return totalValue;
    }
}
