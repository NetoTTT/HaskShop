package com.hask.shop.shop;

public class CashShopItem {

    public final String id;
    public final String customItemId;
    public final double price;
    public final int amount;

    public CashShopItem(String id, String customItemId, double price, int amount) {
        this.id = id;
        this.customItemId = customItemId;
        this.price = price;
        this.amount = amount;
    }

}
