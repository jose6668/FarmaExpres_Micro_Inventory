package co.edu.corhuila.inventory_service.Dto;




import co.edu.corhuila.inventory_service.Entity.Movimiento;

import java.time.LocalDateTime;

public class MovimientoResponse {

    private Long id;
    private String tipo;
    private Integer cantidad;
    private LocalDateTime fecha;
    private Long productoId;

    public Long getId() {
        return id;
    }

    public String getTipo() {
        return tipo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public Long getProductoId() {
        return productoId;
    }

    public MovimientoResponse(Movimiento m) {
        this.id = m.getId();
        this.tipo = m.getTipo().name();
        this.cantidad = m.getCantidad();
        this.fecha = m.getFecha();
        this.productoId = m.getProducto().getId();
    }
}
