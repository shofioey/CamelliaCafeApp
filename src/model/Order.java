package model;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    private String orderId;
    private String buyerUsername;
    private String roomName;
    private List<CartItem> items;
    private double totalAmount;
    private OrderStatus status;
    private long createdTime;

    public Order(String buyerUsername, String roomName, List<CartItem> items) {
        this.orderId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.buyerUsername = buyerUsername;
        this.roomName = roomName;
        this.items = items;
        this.status = OrderStatus.PENDING;
        this.createdTime = System.currentTimeMillis();
        calculateTotal();
    }

    private void calculateTotal() {
        this.totalAmount = items.stream().mapToDouble(CartItem::getTotal).sum();
    }

    public String getOrderId() {
        return orderId;
    }

    public String getBuyerUsername() {
        return buyerUsername;
    }

    public String getRoomName() {
        return roomName;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public String getStatusDisplay() {
        return status.getDisplayName();
    }

    public String getStatusColor() {
        return status.getColor();
    }
}
