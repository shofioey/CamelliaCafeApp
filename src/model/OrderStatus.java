package model;

public enum OrderStatus {
    PENDING("Pending", "#ffc107"),
    PREPARING("Preparing", "#17a2b8"),
    DELIVERED("Delivered", "#28a745"),
    CANCELLED("Cancelled", "#dc3545");

    private final String displayName;
    private final String color;

    OrderStatus(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }
}
