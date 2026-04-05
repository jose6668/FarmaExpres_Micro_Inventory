package co.edu.corhuila.inventory_service.Dto;

public class UserActivityReportResponse {

    private Long userId;
    private String userName;
    private String userRole;
    private Long totalMovements;
    private Long totalEntrances;
    private Long totalExits;
    private String activityLevel;

    public UserActivityReportResponse() {
    }

    public UserActivityReportResponse(Long userId,
                                      String userName,
                                      String userRole,
                                      Long totalMovements,
                                      Long totalEntrances,
                                      Long totalExits,
                                      String activityLevel) {
        this.userId = userId;
        this.userName = userName;
        this.userRole = userRole;
        this.totalMovements = totalMovements;
        this.totalEntrances = totalEntrances;
        this.totalExits = totalExits;
        this.activityLevel = activityLevel;
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

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public Long getTotalMovements() {
        return totalMovements;
    }

    public void setTotalMovements(Long totalMovements) {
        this.totalMovements = totalMovements;
    }

    public Long getTotalEntrances() {
        return totalEntrances;
    }

    public void setTotalEntrances(Long totalEntrances) {
        this.totalEntrances = totalEntrances;
    }

    public Long getTotalExits() {
        return totalExits;
    }

    public void setTotalExits(Long totalExits) {
        this.totalExits = totalExits;
    }

    public String getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(String activityLevel) {
        this.activityLevel = activityLevel;
    }
}
