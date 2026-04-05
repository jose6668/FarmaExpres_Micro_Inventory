package co.edu.corhuila.inventory_service.Dto;

public class LowStockReportItemResponse {

    private final Long id;
    private final String code;
    private final String name;
    private final Integer stock;
    private final Integer minimumStock;
    private final Integer coverage;
    private final String coverageLabel;
    private final String status;
    private final String suggestion;

    public LowStockReportItemResponse(
            Long id,
            String code,
            String name,
            Integer stock,
            Integer minimumStock,
            Integer coverage,
            String coverageLabel,
            String status,
            String suggestion
    ) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.stock = stock;
        this.minimumStock = minimumStock;
        this.coverage = coverage;
        this.coverageLabel = coverageLabel;
        this.status = status;
        this.suggestion = suggestion;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Integer getStock() {
        return stock;
    }

    public Integer getMinimumStock() {
        return minimumStock;
    }

    public Integer getCoverage() {
        return coverage;
    }

    public String getCoverageLabel() {
        return coverageLabel;
    }

    public String getStatus() {
        return status;
    }

    public String getSuggestion() {
        return suggestion;
    }
}
