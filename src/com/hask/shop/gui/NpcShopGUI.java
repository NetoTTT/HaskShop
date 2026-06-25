package com.hask.shop.gui;

import com.hask.shop.HaskShop;
import com.hask.shop.NpcShopItem;
import com.hask.shop.NpcShopManager;
import com.hask.shop.SpawnerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NpcShopGUI {

    public static final String TITLE_PREFIX = "§5NpcShop §8";
    public static final int ITEMS_PER_PAGE = 28;

    static final int[] INNER_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };

    public static void open(Player p, NpcShopManager.NpcShop shop, int page) {
        int totalPages = Math.max(1, (int) Math.ceil((double) shop.items.size() / ITEMS_PER_PAGE));
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        HaskShop.instance.npcShopPage.put(p.getUniqueId(), page);

        Inventory inv = Bukkit.createInventory(null, 54, TITLE_PREFIX + shop.shopId);

        ItemStack glass = build(Material.STAINED_GLASS_PANE, (short) 7, "§r", null);
        for (int i = 0; i < 54; i++) inv.setItem(i, glass);

        inv.setItem(4, build(Material.NETHER_STAR, (short) 0, shop.name,
            Arrays.asList(
                "§7Pagina §f" + (page + 1) + " §7de §f" + totalPages,
                "§7" + shop.items.size() + " item(s) no catalogo",
                "",
                "§a[Esq] §7Comprar  §e[Dir] §7Vender"
            )));

        int start = page * ITEMS_PER_PAGE;
        for (int i = 0; i < INNER_SLOTS.length; i++) {
            int idx = start + i;
            if (idx >= shop.items.size()) break;
            inv.setItem(INNER_SLOTS[i], buildDisplay(shop.items.get(idx)));
        }

        if (page > 0) {
            inv.setItem(45, build(Material.ARROW, (short) 0, "§a« Pagina Anterior",
                Arrays.asList("§7Ir para pagina §f" + page)));
        }
        inv.setItem(49, build(Material.BARRIER, (short) 0, "§cFechar", null));
        if (page < totalPages - 1) {
            inv.setItem(53, build(Material.ARROW, (short) 0, "§aProxima Pagina »",
                Arrays.asList("§7Ir para pagina §f" + (page + 2))));
        }

        p.openInventory(inv);
    }

    private static ItemStack buildDisplay(NpcShopItem item) {
        ItemStack is;
        String displayName;

        if (item.isSpawner()) {
            SpawnerUtil.MobEntry entry = SpawnerUtil.getEntry(item.mobType);
            String ptName = entry != null ? entry.ptName : item.mobType;
            String color  = entry != null ? entry.color  : "§f";
            short eggData = entry != null ? entry.eggData : 0;
            is = new ItemStack(Material.MONSTER_EGG, 1, eggData);
            displayName = color + "§lSpawner de " + ptName;
        } else {
            is = new ItemStack(item.itemType, 1);
            displayName = "§f§l" + formatName(item.itemType.name());
        }

        List<String> lore = new ArrayList<>();
        if (!item.quantityFree) lore.add("§7Quantidade: §f" + item.amount + "x");
        lore.add("");
        if (item.canBuy())  lore.add("§a[Esq] §7Comprar" + (item.quantityFree ? " §8(§7preco por unidade§8)" : " §6" + item.buyPrice + " coins"));
        if (item.canSell()) lore.add("§e[Dir] §7Vender"  + (item.quantityFree ? " §8(§7preco por unidade§8)" : " §6" + item.sellPrice + " coins"));
        if (item.canBuy()  && item.quantityFree) lore.add("    §6" + item.buyPrice  + " coins §7por item");
        if (item.canSell() && item.quantityFree) lore.add("    §6" + item.sellPrice + " coins §7por item");
        if (item.quantityFree) {
            lore.add("");
            lore.add("§b» §7Quantidade livre §8(§fall§8/§ftodos§8/§ftudo§8/§fmax §8= maximo)");
        }

        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        is.setItemMeta(meta);
        return is;
    }

    static String formatName(String raw) {
        String[] parts = raw.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(part.charAt(0)).append(part.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    private static ItemStack build(Material mat, short data, String name, List<String> lore) {
        ItemStack is = new ItemStack(mat, 1, data);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        is.setItemMeta(meta);
        return is;
    }
}
