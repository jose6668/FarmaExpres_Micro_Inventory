package co.edu.corhuila.inventory_service.Dto;

import java.time.LocalDateTime;

public class ApiErrorResponse {

     private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private String service;

    public ApiErrorResponse(LocalDateTime timestamp, int status, String error,
                            String message, String path, String service) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.service = service;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public String getService() {
        return service;
    }

}
