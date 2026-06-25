package com.hask.shop;

public class NpcConfirmSession {
    public final String shopId;
    public final NpcShopItem item;
    public final String transactionType; // "BUY" ou "SELL"
    public final int quantity;           // numero de unidades (units x amount = total itens)

    public NpcConfirmSession(String shopId, NpcShopItem item, String transactionType, int quantity) {
        this.shopId = shopId;
        this.item = item;
        this.transactionType = transactionType;
        this.quantity = quantity;
    }
}
