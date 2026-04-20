package com.example.bigaehrraidapp;

import java.util.Map;

public class StoreItem {

    public static final int TYPE_HEADER  = 0;
    public static final int TYPE_PRODUCT = 1;

    public final int type;
    public String headerName;
    public Map<String, Object> product;

    private StoreItem(int type) {
        this.type = type;
    }

    public static StoreItem header(String name) {
        StoreItem item = new StoreItem(TYPE_HEADER);
        item.headerName = name;
        return item;
    }

    public static StoreItem product(Map<String, Object> product) {
        StoreItem item = new StoreItem(TYPE_PRODUCT);
        item.product = product;
        return item;
    }
}
