package model;

public enum Category {
    MAKANAN("Makanan"),
    MINUMAN("Minuman"),
    SNACK("Snack");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
