package co.edu.corhuila.inventory_service.Entity;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "nombre_generico")
    private String nombreGenerico;

    @Column(name = "concentracion")
    private String concentracion;

    @Column(name = "forma_farmaceutica")
    private String formaFarmaceutica;

    @Column(name = "presentacion")
    private String presentacion;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "unitprice", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "stock_maximo")
    private Integer stockMaximo;

    @Column(name = "precio_compra")
    private BigDecimal precioCompra;

    @Column(name = "precio_venta")
    private BigDecimal precioVenta;

    @Column(name = "requiere_receta")
    private Boolean requiereReceta;

    @Column(name = "laboratorio")
    private String laboratorio;

    @Column(name = "registro_sanitario")
    private String registroSanitario;

    @Column(name = "via_administracion")
    private String viaAdministracion;

    @Column(name = "unidad_medida")
    private String unidadMedida;

    @Column(name = "ubicacion_almacen")
    private String ubicacionAlmacen;

    @Column(name = "temperatura_conservacion")
    private String temperaturaConservacion;

    @Column(name = "observaciones", length = 2000)
    private String observaciones;

    @Column(name = "asset", nullable = false)
    private Boolean active = true;

    @Column(name = "minimumstock", nullable = false)
    private Integer minimumStock;

    @Column(name = "expirationdate", nullable = false)
    private LocalDate expirationDate;

    

    public Product() {
    }

    public Product(String name, String code, Integer stock, BigDecimal unitPrice, LocalDate expirationDate, Integer minimumStock) {
        this.name = name;
        this.code = code;
        this.stock = stock;
        this.unitPrice = unitPrice;
        this.active = true;
        this.expirationDate = expirationDate;
        this.minimumStock = minimumStock;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getNombreGenerico() {
        return nombreGenerico;
    }

    public String getConcentracion() {
        return concentracion;
    }

    public String getFormaFarmaceutica() {
        return formaFarmaceutica;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public Integer getStock() {
        return stock;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public Integer getStockMaximo() {
        return stockMaximo;
    }

    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }

    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }

    public Boolean getRequiereReceta() {
        return requiereReceta;
    }

    public String getLaboratorio() {
        return laboratorio;
    }

    public String getRegistroSanitario() {
        return registroSanitario;
    }

    public String getViaAdministracion() {
        return viaAdministracion;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public String getUbicacionAlmacen() {
        return ubicacionAlmacen;
    }

    public String getTemperaturaConservacion() {
        return temperaturaConservacion;
    }

    public String getObservaciones() {
        return observaciones;
    }


    public LocalDate getExpirationDate() {
        return expirationDate;
    }



    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setNombreGenerico(String nombreGenerico) {
        this.nombreGenerico = nombreGenerico;
    }

    public void setConcentracion(String concentracion) {
        this.concentracion = concentracion;
    }

    public void setFormaFarmaceutica(String formaFarmaceutica) {
        this.formaFarmaceutica = formaFarmaceutica;
    }

    public void setPresentacion(String presentacion) {
        this.presentacion = presentacion;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public void setStockMaximo(Integer stockMaximo) {
        this.stockMaximo = stockMaximo;
    }

    public void setPrecioCompra(BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
    }

    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }

    public void setRequiereReceta(Boolean requiereReceta) {
        this.requiereReceta = requiereReceta;
    }

    public void setLaboratorio(String laboratorio) {
        this.laboratorio = laboratorio;
    }

    public void setRegistroSanitario(String registroSanitario) {
        this.registroSanitario = registroSanitario;
    }

    public void setViaAdministracion(String viaAdministracion) {
        this.viaAdministracion = viaAdministracion;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public void setUbicacionAlmacen(String ubicacionAlmacen) {
        this.ubicacionAlmacen = ubicacionAlmacen;
    }

    public void setTemperaturaConservacion(String temperaturaConservacion) {
        this.temperaturaConservacion = temperaturaConservacion;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }


    public Boolean getActive() {
    return active;
    }

    public void setActive(Boolean active) {
    this.active = active;
    }

    public boolean isActive() {
    return active;
    }

    public Integer getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(Integer minimumStock) {
        this.minimumStock = minimumStock;
    }
}
