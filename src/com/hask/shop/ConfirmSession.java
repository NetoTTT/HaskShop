package com.hask.shop;

public class ConfirmSession {
    public final int shopId;
    public final int quantity;

    public ConfirmSession(int shopId, int quantity) {
        this.shopId = shopId;
        this.quantity = quantity;
    }
}
