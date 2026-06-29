package com.hask.shop.shop;

import com.hask.shop.HaskShop;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class CashShopManager {

    private final HaskShop plugin;
    private final List<CashShopItem> items = new ArrayList<>();

    public CashShopManager(HaskShop plugin) {
        this.plugin = plugin;
    }

    public void load() {
        items.clear();
        File file = new File(plugin.getDataFolder(), "cash-shop.yml");
        if (!file.exists()) {
            plugin.saveResource("cash-shop.yml", false);
        }

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection sec = cfg.getConfigurationSection("items");
        if (sec == null) return;

        for (String id : sec.getKeys(false)) {
            String customItemId = sec.getString(id + ".custom-item");
            if (customItemId == null || !plugin.customItemRegistry.contains(customItemId)) {
                plugin.getLogger().warning("CashShop: item custom '" + customItemId + "' nao encontrado, ignorando '" + id + "'");
                continue;
            }
            double price = sec.getDouble(id + ".price", 0);
            int amount = sec.getInt(id + ".amount", 1);
            items.add(new CashShopItem(id, customItemId, price, amount));
        }

        plugin.getLogger().info("CashShop: " + items.size() + " item(ns) carregado(s).");
    }

    public List<CashShopItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public CashShopItem getItem(int index) {
        if (index < 0 || index >= items.size()) return null;
        return items.get(index);
    }

}
