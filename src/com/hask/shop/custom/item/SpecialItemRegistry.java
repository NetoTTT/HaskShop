package com.hask.shop.custom.item;

import com.hask.shop.HaskShop;

import java.util.HashMap;
import java.util.Map;

public class SpecialItemRegistry {

    private final HaskShop plugin;
    private final Map<String, SpecialItem> items = new HashMap<>();

    public SpecialItemRegistry(HaskShop plugin) {
        this.plugin = plugin;
    }

    public void register(SpecialItem item) {
        items.put(item.getId(), item);
        plugin.getLogger().info("SpecialItem registrado: " + item.getId());
    }

    public SpecialItem get(String id) {
        return items.get(id);
    }

    public boolean contains(String id) {
        return items.containsKey(id);
    }

}
