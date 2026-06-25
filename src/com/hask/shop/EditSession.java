package com.hask.shop;

public class EditSession {
    public final int shopId;
    public final String field; // "item", "price", "amount"

    public EditSession(int shopId, String field) {
        this.shopId = shopId;
        this.field = field;
    }
}
