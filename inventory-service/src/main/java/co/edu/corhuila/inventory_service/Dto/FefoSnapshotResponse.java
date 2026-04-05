package co.edu.corhuila.inventory_service.Dto;

import java.time.OffsetDateTime;
import java.util.List;

public class FefoSnapshotResponse {
    private OffsetDateTime generatedAt;
    private Integer total;
    private List<FefoSnapshotItemResponse> items;

    public FefoSnapshotResponse(OffsetDateTime generatedAt, Integer total, List<FefoSnapshotItemResponse> items) {
        this.generatedAt = generatedAt;
        this.total = total;
        this.items = items;
    }

    public OffsetDateTime getGeneratedAt() {
        return generatedAt;
    }

    public Integer getTotal() {
        return total;
    }

    public List<FefoSnapshotItemResponse> getItems() {
        return items;
    }
}
