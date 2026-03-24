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

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private Integer stock;

    @Column(name = "unitprice", nullable = false)
    private BigDecimal unitPrice;

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

    public Integer getStock() {
        return stock;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
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

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
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
