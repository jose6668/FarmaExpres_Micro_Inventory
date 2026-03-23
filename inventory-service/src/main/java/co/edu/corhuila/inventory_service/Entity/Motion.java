package co.edu.corhuila.inventory_service.Entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "motion")
public class Motion {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private MovementType Type;

    private Integer amount;

    private LocalDateTime dateTime;


    @ManyToOne
    @JoinColumn(name = "produc_id")
    @JsonIgnore
    private Product product;

    public Motion() {}

    public Motion(MovementType Type,
                  Integer amount,
                  Product product) {
        this.Type = Type;
        this.amount = amount;
        this.product = product;
        this.dateTime = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MovementType getType() {
        return Type;
    }

    public void setType(MovementType type) {
        Type = type;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }
}


