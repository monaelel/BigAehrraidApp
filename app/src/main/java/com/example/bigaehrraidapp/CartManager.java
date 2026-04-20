package com.example.bigaehrraidapp;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory cart singleton. Holds items for the currently viewed restaurant.
 * Cleared automatically when the user switches to a different restaurant.
 */
public class CartManager {

    private static CartManager instance;

    private final List<CartItem>       items        = new ArrayList<>();
    private       String               restaurantId = null;
    private       String               restaurantName = null;
    private       OnCartChangedListener listener;

    public interface OnCartChangedListener {
        void onCartChanged(int totalItems);
    }

    private CartManager() {}

    public static synchronized CartManager getInstance() {
        if (instance == null) instance = new CartManager();
        return instance;
    }

    // ── Restaurant context ────────────────────────────────────────────────────

    public void setRestaurant(String id, String name) {
        if (id != null && !id.equals(restaurantId)) {
            // Switching restaurant → clear cart
            items.clear();
        }
        restaurantId   = id;
        restaurantName = name;
        notifyListener();
    }

    public String getRestaurantId()   { return restaurantId; }
    public String getRestaurantName() { return restaurantName; }

    // ── Cart operations ───────────────────────────────────────────────────────

    public void addItem(CartItem newItem) {
        for (CartItem existing : items) {
            if (existing.productId.equals(newItem.productId)) {
                existing.quantity++;
                notifyListener();
                return;
            }
        }
        items.add(newItem);
        notifyListener();
    }

    public void removeItem(String productId) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).productId.equals(productId)) {
                CartItem item = items.get(i);
                if (item.quantity > 1) {
                    item.quantity--;
                } else {
                    items.remove(i);
                }
                notifyListener();
                return;
            }
        }
    }

    public void clearCart() {
        items.clear();
        notifyListener();
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public int getTotalItemCount() {
        int count = 0;
        for (CartItem item : items) count += item.quantity;
        return count;
    }

    public double getSubtotal() {
        double sum = 0;
        for (CartItem item : items) sum += item.lineTotal();
        return sum;
    }

    /** Tax rate: 8 % */
    public double getTaxes() {
        return getSubtotal() * 0.08;
    }

    public double getTotal() {
        return getSubtotal() + getTaxes();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    // ── Observer ──────────────────────────────────────────────────────────────

    public void setOnCartChangedListener(OnCartChangedListener l) {
        listener = l;
    }

    private void notifyListener() {
        if (listener != null) listener.onCartChanged(getTotalItemCount());
    }
}
