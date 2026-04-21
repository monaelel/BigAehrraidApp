package com.example.bigaehrraidapp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton cart that holds items for the current restaurant session.
 * Call {@link #clear()} when the user leaves the restaurant menu.
 */
public class CartManager {

    private static CartManager instance;

    // productId → CartItem
    private final Map<String, CartItem> itemsMap = new LinkedHashMap<>();
    private String restaurantId;
    private String restaurantName;

    private CartManager() {}

    public static synchronized CartManager getInstance() {
        if (instance == null) instance = new CartManager();
        return instance;
    }

    // ── Session ──────────────────────────────────────────────────────────────

    public void setRestaurant(String id, String name) {
        if (!id.equals(restaurantId)) {
            // Switched restaurant — start fresh
            itemsMap.clear();
        }
        this.restaurantId   = id;
        this.restaurantName = name;
    }

    public String getRestaurantId()   { return restaurantId; }
    public String getRestaurantName() { return restaurantName; }

    // ── Mutations ─────────────────────────────────────────────────────────────

    public void addItem(String productId, String name, double price, String imageUrl) {
        if (itemsMap.containsKey(productId)) {
            itemsMap.get(productId).quantity++;
        } else {
            itemsMap.put(productId, new CartItem(productId, name, price, imageUrl));
        }
    }

    public void removeItem(String productId) {
        CartItem item = itemsMap.get(productId);
        if (item == null) return;
        if (item.quantity > 1) {
            item.quantity--;
        } else {
            itemsMap.remove(productId);
        }
    }

    public void clear() {
        itemsMap.clear();
    }

    // ── Reads ─────────────────────────────────────────────────────────────────

    public List<CartItem> getItems() {
        return new ArrayList<>(itemsMap.values());
    }

    public int getTotalCount() {
        int total = 0;
        for (CartItem item : itemsMap.values()) total += item.quantity;
        return total;
    }

    public double getSubtotal() {
        double total = 0;
        for (CartItem item : itemsMap.values()) total += item.subtotal();
        return total;
    }

    public boolean isEmpty() {
        return itemsMap.isEmpty();
    }
}
