package com.example.bigaehrraidapp;

import java.util.ArrayList;
import java.util.List;

public class Category {
    public String id;
    public String name;
    public int sortOrder;

    public Category() {}

    public static List<Category> getFixedCategories() {
        String[] names = {
            "Burgers", "Pizza", "Sushi", "Mexican", "Chinese",
            "Chicken", "Italian", "Healthy & Salads", "Breakfast",
            "Asian Fusion", "Sandwiches", "Desserts"
        };
        List<Category> list = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            Category c = new Category();
            c.id = names[i];
            c.name = names[i];
            c.sortOrder = i;
            list.add(c);
        }
        return list;
    }
}
