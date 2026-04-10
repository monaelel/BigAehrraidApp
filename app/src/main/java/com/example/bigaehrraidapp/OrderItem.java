package com.example.bigaehrraidapp;

public class OrderItem {
    public String name;
    public int    quantity;
    public double price;
    public String imageUrl;

    public OrderItem() {}

    public OrderItem(String name, int quantity, double price, String imageUrl) {
        this.name     = name;
        this.quantity = quantity;
        this.price    = price;
        this.imageUrl = imageUrl;
    }
}
