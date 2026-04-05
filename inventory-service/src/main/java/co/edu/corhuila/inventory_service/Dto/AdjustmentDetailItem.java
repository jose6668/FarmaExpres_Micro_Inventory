package co.edu.corhuila.inventory_service.Dto;

public class AdjustmentDetailItem {

    private String field;
    private String label;
    private Object before;
    private Object after;
    private String format;

    public AdjustmentDetailItem() {
    }

    public AdjustmentDetailItem(String field, String label, Object before, Object after, String format) {
        this.field = field;
        this.label = label;
        this.before = before;
        this.after = after;
        this.format = format;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Object getBefore() {
        return before;
    }

    public void setBefore(Object before) {
        this.before = before;
    }

    public Object getAfter() {
        return after;
    }

    public void setAfter(Object after) {
        this.after = after;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
