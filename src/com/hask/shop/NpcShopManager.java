package com.hask.shop;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class NpcShopManager {

    public static class NpcShop {
        public final String shopId;
        public final String name;
        public final List<NpcShopItem> items;

        public NpcShop(String shopId, String name, List<NpcShopItem> items) {
            this.shopId = shopId;
            this.name = name;
            this.items = Collections.unmodifiableList(items);
        }
    }

    private final HaskShop plugin;
    private final Map<String, NpcShop> shops = new LinkedHashMap<>();
    private File configFile;

    public NpcShopManager(HaskShop plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.getDataFolder().mkdirs();
        configFile = new File(plugin.getDataFolder(), "npc-shops.yml");

        if (!configFile.exists()) {
            try (InputStream in = plugin.getResource("npc-shops.yml")) {
                if (in != null) Files.copy(in, configFile.toPath());
                else configFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Nao foi possivel criar npc-shops.yml: " + e.getMessage());
            }
        }

        shops.clear();
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(configFile);
        ConfigurationSection shopsSec = cfg.getConfigurationSection("shops");
        if (shopsSec == null) return;

        for (String key : shopsSec.getKeys(false)) {
            String name = shopsSec.getString(key + ".name", "§dLoja");
            List<NpcShopItem> items = new ArrayList<>();

            List<?> itemList = shopsSec.getList(key + ".items");
            if (itemList != null) {
                for (Object obj : itemList) {
                    if (!(obj instanceof Map)) continue;
                    Map<?, ?> map = (Map<?, ?>) obj;

                    double buyPrice  = parseDouble(map, "buy_price",  -1);
                    double sellPrice = parseDouble(map, "sell_price", -1);
                    int amount       = parseInt(map, "amount", 1);
                    if (amount < 1) amount = 1;
                    boolean qFree    = "true".equalsIgnoreCase(String.valueOf(map.containsKey("quantity_free") ? map.get("quantity_free") : "false"));

                    if (buyPrice < 0 && sellPrice < 0) {
                        plugin.getLogger().warning("Item sem buy_price nem sell_price em [" + key + "] - ignorado.");
                        continue;
                    }

                    if (map.containsKey("mob")) {
                        String mob = String.valueOf(map.get("mob")).toUpperCase();
                        if (SpawnerUtil.isValid(mob)) {
                            items.add(new NpcShopItem(mob, buyPrice, sellPrice, amount, qFree));
                        } else {
                            plugin.getLogger().warning("Mob invalido em [" + key + "]: " + mob);
                        }
                    } else if (map.containsKey("item")) {
                        String matStr = String.valueOf(map.get("item")).toUpperCase();
                        try {
                            items.add(new NpcShopItem(Material.valueOf(matStr), buyPrice, sellPrice, amount, qFree));
                        } catch (IllegalArgumentException ex) {
                            plugin.getLogger().warning("Material invalido em [" + key + "]: " + matStr);
                        }
                    } else {
                        plugin.getLogger().warning("Item sem 'mob' ou 'item' em [" + key + "] - ignorado.");
                    }
                }
            }

            shops.put(key, new NpcShop(key, name, items));
        }

        plugin.getLogger().info("NpcShopManager: " + shops.size() + " loja(s) carregada(s).");
    }

    public NpcShop getShop(String shopId) { return shops.get(shopId); }
    public Map<String, NpcShop> getAll()  { return Collections.unmodifiableMap(shops); }

    private static double parseDouble(Map<?, ?> map, String key, double def) {
        if (!map.containsKey(key)) return def;
        try { return Double.parseDouble(String.valueOf(map.get(key))); } catch (Exception e) { return def; }
    }

    private static int parseInt(Map<?, ?> map, String key, int def) {
        if (!map.containsKey(key)) return def;
        try { return Integer.parseInt(String.valueOf(map.get(key))); } catch (Exception e) { return def; }
    }
}
