package com.hask.shop;

import com.hask.shop.custom.CustomItemBuilder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ShopManager {
    private final HaskShop plugin;
    private final Map<Integer, ShopData> shops = new LinkedHashMap<>();
    private final Map<String, Integer> locationIndex = new LinkedHashMap<>();
    private int nextId = 1;

    public ShopManager(HaskShop plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "shops.yml");
        if (!file.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        nextId = cfg.getInt("next-id", 1);
        ConfigurationSection sec = cfg.getConfigurationSection("shops");
        if (sec == null) return;
        for (String key : sec.getKeys(false)) {
            int id = Integer.parseInt(key);
            ShopData d = new ShopData();
            d.id = id;
            d.world = sec.getString(key + ".world");
            d.x = sec.getInt(key + ".x");
            d.y = sec.getInt(key + ".y");
            d.z = sec.getInt(key + ".z");
            d.type = sec.getString(key + ".type", "BUY");
            String itemName = sec.getString(key + ".item", "STONE");
            d.item = Material.getMaterial(itemName);
            if (d.item == null) d.item = Material.STONE;
            d.amount = sec.getInt(key + ".amount", 1);
            d.price = sec.getDouble(key + ".price", 0);
            d.enabled = sec.getBoolean(key + ".enabled", true);
            d.askQuantity = sec.getBoolean(key + ".ask-quantity", false);
            d.spawnerType = sec.getString(key + ".spawner-type", null);
            d.customItemId = sec.getString(key + ".custom-item", null);
            if (d.customItemId != null && !plugin.customItemRegistry.contains(d.customItemId)) {
                d.customItemId = null;
            }
            shops.put(id, d);
            locationIndex.put(locKey(d.world, d.x, d.y, d.z), id);
        }
        plugin.getLogger().info("Carregadas " + shops.size() + " lojas.");
    }

    public void save() {
        plugin.getDataFolder().mkdirs();
        FileConfiguration cfg = new YamlConfiguration();
        cfg.set("next-id", nextId);
        for (ShopData d : shops.values()) {
            String path = "shops." + d.id;
            cfg.set(path + ".world", d.world);
            cfg.set(path + ".x", d.x);
            cfg.set(path + ".y", d.y);
            cfg.set(path + ".z", d.z);
            cfg.set(path + ".type", d.type);
            cfg.set(path + ".item", d.item.name());
            cfg.set(path + ".amount", d.amount);
            cfg.set(path + ".price", d.price);
            cfg.set(path + ".enabled", d.enabled);
            cfg.set(path + ".ask-quantity", d.askQuantity);
            if (d.spawnerType != null) cfg.set(path + ".spawner-type", d.spawnerType);
            if (d.customItemId != null) cfg.set(path + ".custom-item", d.customItemId);
        }
        try {
            cfg.save(new File(plugin.getDataFolder(), "shops.yml"));
        } catch (IOException e) {
            plugin.getLogger().warning("Erro ao salvar shops.yml: " + e.getMessage());
        }
    }

    public int register(Location loc) {
        ShopData d = new ShopData();
        d.id = nextId++;
        d.world = loc.getWorld().getName();
        d.x = loc.getBlockX();
        d.y = loc.getBlockY();
        d.z = loc.getBlockZ();
        d.type = "BUY";
        d.item = Material.STONE;
        d.amount = 1;
        d.price = 100;
        d.enabled = false;
        shops.put(d.id, d);
        locationIndex.put(locKey(d.world, d.x, d.y, d.z), d.id);
        save();
        return d.id;
    }

    public boolean remove(int id) {
        ShopData d = shops.remove(id);
        if (d == null) return false;
        locationIndex.remove(locKey(d.world, d.x, d.y, d.z));
        save();
        return true;
    }

    public ShopData getById(int id) {
        return shops.get(id);
    }

    public ShopData getByLocation(Location loc) {
        Integer id = locationIndex.get(locKey(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        return id != null ? shops.get(id) : null;
    }

    public Collection<ShopData> getAll() {
        return shops.values();
    }

    public int count() {
        return shops.size();
    }

    public int freeSpace(Player p, Material mat) {
        int space = 0;
        int maxStack = mat.getMaxStackSize();
        for (ItemStack slot : p.getInventory().getContents()) {
            if (slot == null || slot.getType() == Material.AIR) {
                space += maxStack;
            } else if (slot.getType() == mat && slot.getAmount() < maxStack) {
                space += maxStack - slot.getAmount();
            }
        }
        return space;
    }

    public String getItemDisplayName(ShopData shop) {
        if (shop.customItemId != null) {
            ItemStack ci = plugin.customItemRegistry.get(shop.customItemId);
            if (ci != null && ci.hasItemMeta() && ci.getItemMeta().hasDisplayName()) {
                return ci.getItemMeta().getDisplayName();
            }
            return shop.customItemId;
        }
        if (shop.spawnerType != null) return "Spawner de " + shop.spawnerType;
        return shop.item.name().replace("_", " ");
    }

    public boolean executeBuy(Player p, ShopData shop, int qty) {
        int totalItems = shop.amount * qty;
        double totalPrice = shop.price * qty;

        if (HaskShop.economy.getBalance(p.getName()) < totalPrice) {
            p.sendMessage("\u00A7cCoins insuficientes! Necessario: \u00A7f" + String.format("%.2f", totalPrice) + " coins.");
            return false;
        }

        ItemStack[] toGive;
        if (shop.customItemId != null) {
            ItemStack template = plugin.customItemRegistry.get(shop.customItemId);
            if (template == null) {
                p.sendMessage("\u00A7cItem custom \u00A7f" + shop.customItemId + " \u00A7cnao encontrado no registro.");
                return false;
            }
            int maxStack = template.getMaxStackSize();
            int numStacks = (int) Math.ceil((double) totalItems / maxStack);
            toGive = new ItemStack[numStacks];
            for (int i = 0; i < numStacks; i++) {
                ItemStack clone = template.clone();
                clone.setAmount(Math.min(totalItems - i * maxStack, maxStack));
                toGive[i] = clone;
            }
        } else if (shop.item == Material.MOB_SPAWNER && shop.spawnerType != null) {
            toGive = SpawnerUtil.createStacks(shop.spawnerType, totalItems);
        } else {
            int maxStack = shop.item.getMaxStackSize();
            int numStacks = (int) Math.ceil((double) totalItems / maxStack);
            toGive = new ItemStack[numStacks];
            for (int i = 0; i < numStacks; i++) {
                toGive[i] = new ItemStack(shop.item, Math.min(totalItems - i * maxStack, maxStack));
            }
        }

        if (freeSpace(p, shop.item) < totalItems) {
            p.sendMessage("\u00A7cInventario cheio! Libere espaco para \u00A7f" + totalItems + " \u00A7citens.");
            return false;
        }

        HaskShop.economy.withdrawPlayer(p.getName(), totalPrice);
        p.getInventory().addItem(toGive);
        p.sendMessage("\u00A7aCompra realizada! \u00A7f" + totalItems + "x " + getItemDisplayName(shop) + " \u00A7apor \u00A7f" + String.format("%.2f", totalPrice) + " coins.");
        return true;
    }

    public boolean executeSell(Player p, ShopData shop, int qty) {
        int totalItems = shop.amount * qty;
        double totalPrice = shop.price * qty;

        int count = 0;
        Material mat = shop.customItemId != null ? plugin.customItemRegistry.get(shop.customItemId).getType() : shop.item;
        for (ItemStack is : p.getInventory().getContents()) {
            if (is != null && is.getType() == mat) {
                if (shop.customItemId != null) {
                    String id = CustomItemBuilder.getId(is);
                    if (shop.customItemId.equals(id)) {
                        count += is.getAmount();
                    }
                } else {
                    count += is.getAmount();
                }
            }
        }
        if (count < totalItems) {
            p.sendMessage("\u00A7cVoce precisa de \u00A7f" + totalItems + "x " + getItemDisplayName(shop) + " \u00A7cpara vender.");
            return false;
        }

        int toRemove = totalItems;
        for (ItemStack is : p.getInventory().getContents()) {
            if (toRemove <= 0) break;
            if (is == null || is.getType() != mat) continue;
            if (shop.customItemId != null) {
                String id = CustomItemBuilder.getId(is);
                if (!shop.customItemId.equals(id)) continue;
            }
            int remove = Math.min(is.getAmount(), toRemove);
            is.setAmount(is.getAmount() - remove);
            toRemove -= remove;
        }
        p.updateInventory();

        HaskShop.economy.depositPlayer(p.getName(), totalPrice);
        p.sendMessage("\u00A7aVenda realizada! \u00A7f" + totalItems + "x " + getItemDisplayName(shop) + " \u00A7apor \u00A7f" + String.format("%.2f", totalPrice) + " coins.");
        return true;
    }

    public ItemStack[] buildStacks(Material mat, int total) {
        int maxStack = mat.getMaxStackSize();
        int numStacks = (int) Math.ceil((double) total / maxStack);
        ItemStack[] stacks = new ItemStack[numStacks];
        for (int i = 0; i < numStacks; i++) {
            stacks[i] = new ItemStack(mat, Math.min(total - i * maxStack, maxStack));
        }
        return stacks;
    }

    private String locKey(String world, int x, int y, int z) {
        return world + "," + x + "," + y + "," + z;
    }
}
