package co.edu.corhuila.inventory_service.Dto;

import co.edu.corhuila.inventory_service.Entity.Producto;

public class ProductoSinStockResponse {

    private Long id;
    private String nombre;
    private String codigo;
    private Integer stock;

    public ProductoSinStockResponse(Producto producto) {
        this.id = producto.getId();
        this.nombre = producto.getNombre();
        this.codigo = producto.getCodigo();
        this.stock = producto.getStock();
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCodigo() { return codigo; }
    public Integer getStock() { return stock; }
}
