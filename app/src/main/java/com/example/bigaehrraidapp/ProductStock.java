package com.example.bigaehrraidapp;

public class ProductStock {
    String name;
    int stockCount;
    double pricePerUnit;
    String ingredients;

    public ProductStock(String name, int stockCount, double pricePerUnit, String ingredients) {
        this.name = name;
        this.stockCount = stockCount;
        this.pricePerUnit = pricePerUnit;
        this.ingredients = ingredients;
    }
}
