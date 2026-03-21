package co.edu.corhuila.inventory_service.Entity;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true, nullable = false)
    private String codigo;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private BigDecimal precio;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "fechavencimiento", nullable = false)
    private LocalDate fechavencimiento;


    public Producto() {}

    public Producto(String nombre, String codigo, Integer stock, BigDecimal precio, LocalDate fechavencimiento) {
        this.nombre = nombre;
        this.codigo = codigo;
        this.stock = stock;
        this.precio = precio;
        this.activo = true;
        this.fechavencimiento = fechavencimiento;

    }

    public void aumentarStock(Integer cantidad) {
        this.stock += cantidad;
    }

    public void disminuirStock(Integer cantidad) {
        if (this.stock < cantidad) {
            throw new RuntimeException("Stock insuficiente");
        }
        this.stock -= cantidad;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCodigo() { return codigo; }
    public Integer getStock() { return stock; }
    public BigDecimal getPrecio() { return precio; }
    public Boolean getActivo() { return activo; }

    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }

    public void setStock(Integer stock) {
        this.stock = stock;
    }


    public LocalDate getFechavencimiento() {
        return fechavencimiento;
    }

    public void setFechavencimiento(LocalDate fechavencimiento) {
        this.fechavencimiento = fechavencimiento;
    }

    public void setId(Long id) {
        this.id = id;
    }
    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
