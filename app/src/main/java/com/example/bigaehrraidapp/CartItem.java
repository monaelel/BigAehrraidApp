package com.example.bigaehrraidapp;

public class CartItem {
    public String productId;
    public String name;
    public String imageUrl;
    public double price;
    public int    quantity;

    public CartItem() {}

    public CartItem(String productId, String name, String imageUrl, double price) {
        this.productId = productId;
        this.name      = name;
        this.imageUrl  = imageUrl;
        this.price     = price;
        this.quantity  = 1;
    }

    public double lineTotal() {
        return price * quantity;
    }
}
