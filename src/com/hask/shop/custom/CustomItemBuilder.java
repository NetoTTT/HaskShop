package com.hask.shop.custom;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CustomItemBuilder {

    private String id;
    private Material material = Material.PAPER;
    private int amount = 1;
    private short data = 0;
    private String displayName;
    private List<String> lore = new ArrayList<>();
    private Map<Enchantment, Integer> enchantments = new LinkedHashMap<>();
    private Map<String, String>  strNbt = new LinkedHashMap<>();
    private Map<String, Integer> intNbt = new LinkedHashMap<>();

    public CustomItemBuilder id(String id)             { this.id = id; return this; }
    public CustomItemBuilder material(Material m)      { this.material = m; return this; }
    public CustomItemBuilder amount(int a)             { this.amount = a; return this; }
    public CustomItemBuilder data(short d)             { this.data = d; return this; }
    public CustomItemBuilder name(String name)         { this.displayName = name; return this; }
    public CustomItemBuilder lore(String... lines)     { this.lore.addAll(Arrays.asList(lines)); return this; }
    public CustomItemBuilder lore(List<String> lines)  { this.lore.addAll(lines); return this; }
    public CustomItemBuilder enchant(Enchantment e, int lvl) { enchantments.put(e, lvl); return this; }
    public CustomItemBuilder nbt(String key, String v) { strNbt.put(key, v); return this; }
    public CustomItemBuilder nbt(String key, int v)    { intNbt.put(key, v); return this; }

    public ItemStack build() {
        // 1. Cria item base com nome e lore
        ItemStack item = new ItemStack(material, amount, data);
        ItemMeta meta = item.getItemMeta();
        if (displayName != null) meta.setDisplayName(displayName);
        if (!lore.isEmpty()) meta.setLore(lore);
        item.setItemMeta(meta);

        // 2. Converte para NMS e adiciona tags customizadas (hask_id, usos, etc)
        net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nms.getTag() != null ? nms.getTag() : new NBTTagCompound();
        if (id != null) tag.setString("hask_id", id);
        for (Map.Entry<String, String>  e : strNbt.entrySet()) tag.setString(e.getKey(), e.getValue());
        for (Map.Entry<String, Integer> e : intNbt.entrySet())  tag.setInt(e.getKey(), e.getValue());
        nms.setTag(tag);
        item = CraftItemStack.asCraftMirror(nms);

        // 3. Aplica encantamento via addUnsafeEnchantment DEPOIS do NMS
        //    (essencial para SkullMeta que perde enchant no fluxo NMS)
        if (!enchantments.isEmpty()) {
            for (Map.Entry<Enchantment, Integer> e : enchantments.entrySet()) {
                item.addUnsafeEnchantment(e.getKey(), e.getValue());
            }
            ItemMeta enchMeta = item.getItemMeta();
            enchMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(enchMeta);
        }

        return item;
    }

    // --- Helpers estaticos ---

    public static String getId(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        try {
            net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
            NBTTagCompound tag = nms.getTag();
            if (tag != null && tag.hasKey("hask_id")) return tag.getString("hask_id");
        } catch (Exception ignored) {}
        return null;
    }

    public static int getNbtInt(ItemStack item, String key) {
        if (item == null) return -1;
        try {
            net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
            NBTTagCompound tag = nms.getTag();
            if (tag != null && tag.hasKey(key)) return tag.getInt(key);
        } catch (Exception ignored) {}
        return -1;
    }

    public static ItemStack setNbtInt(ItemStack item, String key, int value) {
        net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = nms.getTag() != null ? nms.getTag() : new NBTTagCompound();
        tag.setInt(key, value);
        nms.setTag(tag);
        return CraftItemStack.asBukkitCopy(nms);
    }
}
