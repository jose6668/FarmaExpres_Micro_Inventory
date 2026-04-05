package co.edu.corhuila.inventory_service.Dto;




import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import co.edu.corhuila.inventory_service.Entity.Motion;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class MotionResponse {

    private Long id;
    private Instant dateTime;
    private String type;
    private Integer amount;
    private Long productId;
    private String productName;
    private Long batchId;
    private String batchCode;
    private LocalDate batchExpirationDate;
    private String reason;
    private String detail;
    private Long userId;
    private String userName;
    private String userEmail;
    private String userRole;
    private String status;
    private Long markedByUserId;
    private String markedByUserName;
    private Instant markedAt;
    private String observation;
    private String adjustmentSummary;
    private List<AdjustmentDetailItem> adjustmentDetail;

    private static final String SYSTEM_USER_NAME = "SYSTEM_INIT";
    private static final String SYSTEM_USER_EMAIL = "system@farmaexpres.local";
    private static final String SYSTEM_USER_ROLE = "SYSTEM";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
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

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public String getBatchCode() {
        return batchCode;
    }

    public void setBatchCode(String batchCode) {
        this.batchCode = batchCode;
    }

    public LocalDate getBatchExpirationDate() {
        return batchExpirationDate;
    }

    public void setBatchExpirationDate(LocalDate batchExpirationDate) {
        this.batchExpirationDate = batchExpirationDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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

    public List<AdjustmentDetailItem> getAdjustmentDetail() {
        return adjustmentDetail;
    }

    public void setAdjustmentDetail(List<AdjustmentDetailItem> adjustmentDetail) {
        this.adjustmentDetail = adjustmentDetail;
    }

    public MotionResponse(Motion m) {
        this.id = m.getId();
        this.type = m.getType().name();
        this.amount = m.getAmount();
        this.dateTime = m.getDateTime();
        if (m.getProduct() != null) {
            this.productId = m.getProduct().getId();
            this.productName = m.getProduct().getName();
        }
        if (m.getBatch() != null) {
            this.batchId = m.getBatch().getId();
            this.batchCode = m.getBatch().getBatchCode();
            this.batchExpirationDate = m.getBatch().getExpirationDate();
        }
        this.reason = m.getReason();
        this.detail = m.getObservation();
        this.userId = m.getUserId();
        this.userName = firstNotBlank(m.getUserName(), SYSTEM_USER_NAME);
        this.userEmail = firstNotBlank(m.getUserEmail(), SYSTEM_USER_EMAIL);
        this.userRole = firstNotBlank(m.getUserRole(), SYSTEM_USER_ROLE);
        this.status = m.getStatus() != null ? m.getStatus().name() : "NORMAL";
        this.markedByUserId = m.getMarkedByUserId();
        this.markedByUserName = m.getMarkedByUserName();
        this.markedAt = m.getMarkedAt();
        this.observation = m.getObservation();
        this.adjustmentSummary = m.getAdjustmentSummary();
        this.adjustmentDetail = parseAdjustmentDetail(m.getAdjustmentDetail());
    }

    private String firstNotBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private List<AdjustmentDetailItem> parseAdjustmentDetail(String adjustmentDetailJson) {
        if (adjustmentDetailJson == null || adjustmentDetailJson.isBlank()) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(
                    adjustmentDetailJson,
                    new TypeReference<List<AdjustmentDetailItem>>() {}
            );
        } catch (Exception ex) {
            return null;
        }
    }
}
