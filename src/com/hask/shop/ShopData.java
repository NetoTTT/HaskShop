package com.hask.shop;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

public class ShopData {
    public int id;
    public String world;
    public int x, y, z;
    public String type; // "BUY" or "SELL"
    public Material item;
    public int amount;
    public double price;
    public boolean enabled;
    public boolean askQuantity;
    public String spawnerType; // null se nao for spawner, ex: "ZOMBIE", "BLAZE"
    public String customItemId; // null se nao for item custom, ex: "gerador_bedrock"

    public Location getLocation() {
        return new Location(Bukkit.getWorld(world), x, y, z);
    }
}
