package com.example.bigaehrraidapp;

public class CartItem {
    public String productId;
    public String name;
    public double price;
    public String imageUrl;
    public int    quantity;

    public CartItem(String productId, String name, double price, String imageUrl) {
        this.productId = productId;
        this.name      = name;
        this.price     = price;
        this.imageUrl  = imageUrl;
        this.quantity  = 1;
    }

    public double subtotal() {
        return price * quantity;
    }
}
