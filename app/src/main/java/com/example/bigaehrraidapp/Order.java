package com.example.bigaehrraidapp;

public class Order {

    public static final String STATUS_INCOMING  = "Incoming";
    public static final String STATUS_PREPARING = "Preparing";
    public static final String STATUS_COMPLETED = "Completed";

    public String orderId;
    public String customerName;
    public int    itemCount;
    public double totalAmount;
    public String status;

    public Order(String orderId, String customerName, int itemCount,
                 double totalAmount, String status) {
        this.orderId       = orderId;
        this.customerName  = customerName;
        this.itemCount     = itemCount;
        this.totalAmount   = totalAmount;
        this.status        = status;
    }
}
