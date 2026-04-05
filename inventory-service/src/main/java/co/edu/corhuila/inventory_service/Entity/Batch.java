package co.edu.corhuila.inventory_service.Entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "batch",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_batch_product_code",
                columnNames = {"product_id", "batch_code"}
        )
)
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "batch_code", nullable = false, length = 100)
    private String batchCode;

    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Column(name = "initial_stock", nullable = false)
    private Integer initialStock;

    @Column(name = "available_stock", nullable = false)
    private Integer availableStock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BatchStatus status = BatchStatus.ACTIVE;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void beforeInsert() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        recalculateStatus();
    }

    @PreUpdate
    void beforeUpdate() {
        this.updatedAt = Instant.now();
        recalculateStatus();
    }

    public void recalculateStatus() {
        if (this.status == BatchStatus.RETIRED) {
            return;
        }

        if (this.availableStock == null) {
            this.availableStock = 0;
        }

        if (this.availableStock < 0) {
            throw new IllegalStateException("availableStock no puede ser negativo");
        }

        if (this.expirationDate != null && this.expirationDate.isBefore(LocalDate.now())) {
            this.status = BatchStatus.EXPIRED;
            return;
        }

        if (this.availableStock == 0) {
            this.status = BatchStatus.OUT_OF_STOCK;
            return;
        }

        this.status = BatchStatus.ACTIVE;
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public Integer getInitialStock() {
        return initialStock;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }

    public BatchStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setBatchCode(String batchCode) {
        this.batchCode = batchCode;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public void setInitialStock(Integer initialStock) {
        this.initialStock = initialStock;
    }

    public void setAvailableStock(Integer availableStock) {
        this.availableStock = availableStock;
    }

    public void setStatus(BatchStatus status) {
        this.status = status;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

