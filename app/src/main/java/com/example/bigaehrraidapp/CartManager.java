package com.example.bigaehrraidapp;

import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static CartManager instance;
    private String restaurantId;
    private final List<CartItem> items;
    private OnCartChangeListener listener;

    public interface OnCartChangeListener {
        void onCartChanged();
    }

    private CartManager() {
        items = new ArrayList<>();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setListener(OnCartChangeListener listener) {
        this.listener = listener;
    }

    public void addItem(String productId, String name, double price, String imageUrl) {
        for (CartItem item : items) {
            if (item.getProductId() != null && item.getProductId().equals(productId)) {
                item.setQuantity(item.getQuantity() + 1);
                notifyListener();
                return;
            }
        }
        items.add(new CartItem(productId, name, price, imageUrl));
        notifyListener();
    }

    public void updateQuantity(String productId, int newQuantity) {
        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            if (item.getProductId() != null && item.getProductId().equals(productId)) {
                if (newQuantity <= 0) {
                    items.remove(i);
                } else {
                    item.setQuantity(newQuantity);
                }
                notifyListener();
                return;
            }
        }
    }

    public void removeItem(String productId) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getProductId() != null && items.get(i).getProductId().equals(productId)) {
                items.remove(i);
                notifyListener();
                return;
            }
        }
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items); // return a copy
    }

    public double getSubtotal() {
        double total = 0;
        for (CartItem item : items) {
            total += item.getPrice() * item.getQuantity();
        }
        return total;
    }

    public int getTotalItemCount() {
        int count = 0;
        for (CartItem item : items) {
            count += item.getQuantity();
        }
        return count;
    }

    public void clearCart() {
        items.clear();
        notifyListener();
    }

    private void notifyListener() {
        if (listener != null) {
            listener.onCartChanged();
        }
    }
}
