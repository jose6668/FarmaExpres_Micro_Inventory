package co.edu.corhuila.inventory_service.Entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "motion")
public class Motion {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private MovementType type;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "date_time", nullable = false)
    private Instant dateTime;

    @Column(name = "reason")
    private String reason;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "user_role")
    private String userRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MotionStatus status = MotionStatus.NORMAL;

    @Column(name = "marked_by_user_id")
    private Long markedByUserId;

    @Column(name = "marked_by_user_name")
    private String markedByUserName;

    @Column(name = "marked_at")
    private Instant markedAt;

    @Column(name = "observation")
    private String observation;

    @Column(name = "adjustment_summary")
    private String adjustmentSummary;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "adjustment_detail", columnDefinition = "jsonb")
    private String adjustmentDetail;

    @ManyToOne
    @JoinColumn(name = "produc_id")
    @JsonIgnore
    private Product product;

    @ManyToOne
    @JoinColumn(name = "batch_id")
    @JsonIgnore
    private Batch batch;

    public Motion() {}

    public Motion(MovementType type,
                  Integer amount,
                  Product product) {
        this(type, amount, product, null, null, null, null, null);
    }

    public Motion(MovementType type,
                  Integer amount,
                  Product product,
                  String reason,
                  Long userId,
                  String userName,
                  String userEmail,
                  String userRole) {
        this.type = type;
        this.amount = amount;
        this.product = product;
        this.dateTime = Instant.now();
        this.reason = reason;
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userRole = userRole;
        this.status = MotionStatus.NORMAL;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MovementType getType() {
        return type;
    }

    public void setType(MovementType type) {
        this.type = type;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Instant getDateTime() {
        return dateTime;
    }

    public void setDateTime(Instant dateTime) {
        this.dateTime = dateTime;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public MotionStatus getStatus() {
        return status;
    }

    public void setStatus(MotionStatus status) {
        this.status = status;
    }

    public Long getMarkedByUserId() {
        return markedByUserId;
    }

    public void setMarkedByUserId(Long markedByUserId) {
        this.markedByUserId = markedByUserId;
    }

    public String getMarkedByUserName() {
        return markedByUserName;
    }

    public void setMarkedByUserName(String markedByUserName) {
        this.markedByUserName = markedByUserName;
    }

    public Instant getMarkedAt() {
        return markedAt;
    }

    public void setMarkedAt(Instant markedAt) {
        this.markedAt = markedAt;
    }

    public String getObservation() {
        return observation;
    }

    public void setObservation(String observation) {
        this.observation = observation;
    }

    public String getAdjustmentSummary() {
        return adjustmentSummary;
    }

    public void setAdjustmentSummary(String adjustmentSummary) {
        this.adjustmentSummary = adjustmentSummary;
    }

    public String getAdjustmentDetail() {
        return adjustmentDetail;
    }

    public void setAdjustmentDetail(String adjustmentDetail) {
        this.adjustmentDetail = adjustmentDetail;
    }

    public Batch getBatch() {
        return batch;
    }

    public void setBatch(Batch batch) {
        this.batch = batch;
    }
}


