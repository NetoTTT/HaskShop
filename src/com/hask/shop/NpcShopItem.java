package com.hask.shop;

import org.bukkit.Material;

public class NpcShopItem {
    public final String mobType;       // non-null = spawner
    public final Material itemType;
    public final double buyPrice;      // -1 = nao compravel
    public final double sellPrice;     // -1 = nao vendavel
    public final int amount;           // unidade base por transacao
    public final boolean quantityFree; // player escolhe a quantidade

    // Spawner
    public NpcShopItem(String mobType, double buyPrice, double sellPrice, int amount, boolean quantityFree) {
        this.mobType = mobType;
        this.itemType = Material.MOB_SPAWNER;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.amount = amount;
        this.quantityFree = quantityFree;
    }

    // Item comum
    public NpcShopItem(Material itemType, double buyPrice, double sellPrice, int amount, boolean quantityFree) {
        this.mobType = null;
        this.itemType = itemType;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.amount = amount;
        this.quantityFree = quantityFree;
    }

    public boolean isSpawner() { return mobType != null; }
    public boolean canBuy()    { return buyPrice  >= 0; }
    public boolean canSell()   { return sellPrice >= 0; }
}
