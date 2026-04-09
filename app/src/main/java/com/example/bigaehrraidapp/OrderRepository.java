package com.example.bigaehrraidapp;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderRepository {

    private static OrderRepository instance;
    private final FirebaseFirestore  db;
    private final AuthRepository     authRepo;
    private ListenerRegistration     ordersListener;

    public interface OrdersCallback {
        void onOrdersUpdated(List<Order> orders);
        void onFailure(String error);
    }

    public interface ActionCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface StatsCallback {
        void onSuccess(double totalSales, int orderVolume, double ticketSize, float[] hourlyData);
        void onFailure(String error);
    }

    private OrderRepository(AuthRepository authRepo) {
        this.db       = FirebaseFirestore.getInstance();
        this.authRepo = authRepo;
    }

    public static synchronized OrderRepository getInstance(AuthRepository authRepo) {
        if (instance == null) instance = new OrderRepository(authRepo);
        return instance;
    }

    // ── Real-time orders listener ─────────────────────────────────────────────

    public void listenToOrders(OrdersCallback cb) {
        String restaurantId = authRepo.getCurrentUserId();
        if (restaurantId == null) { cb.onFailure("Not logged in"); return; }

        ordersListener = db.collection("orders")
            .whereEqualTo("restaurantId", restaurantId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener((snapshots, error) -> {
                if (error != null) { cb.onFailure(error.getMessage()); return; }
                List<Order> orders = new ArrayList<>();
                if (snapshots != null) {
                    for (QueryDocumentSnapshot doc : snapshots) {
                        String status = doc.getString("status");
                        if (Order.STATUS_DECLINED.equals(status)) continue;

                        Order o = new Order();
                        o.orderId      = doc.getId();
                        o.restaurantId = doc.getString("restaurantId");
                        o.customerId   = doc.getString("customerId");
                        o.customerName = doc.getString("customerName");
                        o.status       = status != null ? status : Order.STATUS_INCOMING;
                        o.totalAmount  = doc.getDouble("total")     != null ? doc.getDouble("total")  : 0;
                        o.itemCount    = doc.getLong("itemCount")   != null ? doc.getLong("itemCount").intValue() : 0;
                        o.createdAt    = doc.getLong("createdAt")   != null ? doc.getLong("createdAt") : 0;
                        orders.add(o);
                    }
                }
                cb.onOrdersUpdated(orders);
            });
    }

    public void removeListener() {
        if (ordersListener != null) { ordersListener.remove(); ordersListener = null; }
    }

    // ── Update order status ───────────────────────────────────────────────────

    public void updateOrderStatus(String orderId, String newStatus, ActionCallback cb) {
        Map<String, Object> update = new HashMap<>();
        update.put("status",    newStatus);
        update.put("updatedAt", System.currentTimeMillis());

        db.collection("orders").document(orderId)
          .update(update)
          .addOnSuccessListener(v -> cb.onSuccess())
          .addOnFailureListener(e -> cb.onFailure(e.getMessage()));
    }

    // ── Today's sales stats (for Home dashboard) ──────────────────────────────

    public void getTodayStats(String restaurantId, StatsCallback cb) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfDay = cal.getTimeInMillis();

        db.collection("orders")
          .whereEqualTo("restaurantId", restaurantId)
          .whereGreaterThanOrEqualTo("createdAt", startOfDay)
          .get()
          .addOnSuccessListener(snapshots -> {
              double  totalSales  = 0;
              int     orderVolume = 0;
              float[] hourly      = new float[24];

              for (QueryDocumentSnapshot doc : snapshots) {
                  String status = doc.getString("status");
                  if (Order.STATUS_DECLINED.equals(status)) continue;

                  Double amount = doc.getDouble("total");
                  if (amount == null) amount = 0.0;
                  totalSales  += amount;
                  orderVolume += 1;

                  Long ts = doc.getLong("createdAt");
                  if (ts != null) {
                      Calendar c = Calendar.getInstance();
                      c.setTimeInMillis(ts);
                      int hour = c.get(Calendar.HOUR_OF_DAY);
                      hourly[hour] += amount.floatValue();
                  }
              }

              double ticketSize = orderVolume > 0 ? totalSales / orderVolume : 0;
              cb.onSuccess(totalSales, orderVolume, ticketSize, hourly);
          })
          .addOnFailureListener(e -> cb.onFailure(e.getMessage()));
    }
}
