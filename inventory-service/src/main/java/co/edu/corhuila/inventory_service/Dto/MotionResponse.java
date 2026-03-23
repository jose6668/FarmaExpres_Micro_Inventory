package co.edu.corhuila.inventory_service.Dto;




import co.edu.corhuila.inventory_service.Entity.Motion;

import java.time.LocalDateTime;

public class MotionResponse {

    private Long id;
    private String Type;
    private Integer amount;
    private LocalDateTime date;
    private Long productId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public MotionResponse(Motion m) {
        this.id = m.getId();
        this.Type = m.getType().name();
        this.amount = m.getAmount();
        this.date = m.getDateTime();
        this.productId = m.getProduct().getId();
    }
}
