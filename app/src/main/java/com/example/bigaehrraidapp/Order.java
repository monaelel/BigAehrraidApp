package com.example.bigaehrraidapp;

public class Order {

    public static final String STATUS_INCOMING  = "Incoming";
    public static final String STATUS_PREPARING = "Preparing";
    public static final String STATUS_COMPLETED = "Completed";
    public static final String STATUS_DECLINED  = "Declined";

    public String orderId;
    public String restaurantId;
    public String customerId;
    public String customerName;
    public String customerPhone;
    public String customerAddress;
    public int    itemCount;
    public double totalAmount;
    public double taxes;
    public String status;
    public long   createdAt;

    // No-arg constructor required by Firestore
    public Order() {}

    // Convenience constructor (used for local testing / manual creation)
    public Order(String orderId, String customerName, int itemCount,
                 double totalAmount, String status) {
        this.orderId      = orderId;
        this.customerName = customerName;
        this.itemCount    = itemCount;
        this.totalAmount  = totalAmount;
        this.status       = status;
    }
}
