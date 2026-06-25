package com.hask.shop;

public class NpcQtySession {
    public final String shopId;
    public final NpcShopItem item;
    public final String transactionType; // "BUY" ou "SELL"

    public NpcQtySession(String shopId, NpcShopItem item, String transactionType) {
        this.shopId = shopId;
        this.item = item;
        this.transactionType = transactionType;
    }
}
