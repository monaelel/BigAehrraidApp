package com.example.bigaehrraidapp;

public class CartItem {
    private String productId;
    private String name;
    private double price;
    private int quantity;
    private String imageUrl;

    public CartItem(String productId, String name, double price, String imageUrl) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.quantity = 1;
    }

    public String getProductId() { return productId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getImageUrl() { return imageUrl; }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
