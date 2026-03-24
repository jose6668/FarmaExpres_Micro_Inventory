package co.edu.corhuila.inventory_service.Dto;

import co.edu.corhuila.inventory_service.Entity.Product;

public class ProductOutOfStockResponse {

    private Long id;
    private String name;
    private String code;
    private Integer stock;
    private Integer minimumStock;

    public ProductOutOfStockResponse(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.code = product.getCode();
        this.stock = product.getStock();
        this.minimumStock = product.getMinimumStock();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public Integer getStock() { return stock; }
    public Integer getMinimumStock() {
        return minimumStock;
    }
}
