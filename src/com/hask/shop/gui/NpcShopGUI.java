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

    public static final String TITLE_PREFIX = "\u00A75NpcShop \u00A78";
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

        ItemStack glass = build(Material.STAINED_GLASS_PANE, (short) 7, "\u00A7r", null);
        for (int i = 0; i < 54; i++) inv.setItem(i, glass);

        inv.setItem(4, build(Material.NETHER_STAR, (short) 0, shop.name,
            Arrays.asList(
                "\u00A77Pagina \u00A7f" + (page + 1) + " \u00A77de \u00A7f" + totalPages,
                "\u00A77" + shop.items.size() + " item(s) no catalogo",
                "",
                "\u00A7a[Esq] \u00A77Comprar  \u00A7e[Dir] \u00A77Vender"
            )));

        int start = page * ITEMS_PER_PAGE;
        for (int i = 0; i < INNER_SLOTS.length; i++) {
            int idx = start + i;
            if (idx >= shop.items.size()) break;
            inv.setItem(INNER_SLOTS[i], buildDisplay(shop.items.get(idx)));
        }

        if (page > 0) {
            inv.setItem(45, build(Material.ARROW, (short) 0, "\u00A7a« Pagina Anterior",
                Arrays.asList("\u00A77Ir para pagina \u00A7f" + page)));
        }
        inv.setItem(49, build(Material.BARRIER, (short) 0, "\u00A7cFechar", null));
        if (page < totalPages - 1) {
            inv.setItem(53, build(Material.ARROW, (short) 0, "\u00A7aProxima Pagina »",
                Arrays.asList("\u00A77Ir para pagina \u00A7f" + (page + 2))));
        }

        p.openInventory(inv);
    }

    private static ItemStack buildDisplay(NpcShopItem item) {
        ItemStack is;
        String displayName;

        if (item.isSpawner()) {
            SpawnerUtil.MobEntry entry = SpawnerUtil.getEntry(item.mobType);
            String ptName = entry != null ? entry.ptName : item.mobType;
            String color  = entry != null ? entry.color  : "\u00A7f";
            short eggData = entry != null ? entry.eggData : 0;
            is = new ItemStack(Material.MONSTER_EGG, 1, eggData);
            displayName = color + "\u00A7lSpawner de " + ptName;
        } else {
            is = new ItemStack(item.itemType, 1);
            displayName = "\u00A7f\u00A7l" + formatName(item.itemType.name());
        }

        List<String> lore = new ArrayList<>();
        if (!item.quantityFree) lore.add("\u00A77Quantidade: \u00A7f" + item.amount + "x");
        lore.add("");
        if (item.canBuy())  lore.add("\u00A7a[Esq] \u00A77Comprar" + (item.quantityFree ? " \u00A78(\u00A77preco por unidade\u00A78)" : " \u00A76" + item.buyPrice + " coins"));
        if (item.canSell()) lore.add("\u00A7e[Dir] \u00A77Vender"  + (item.quantityFree ? " \u00A78(\u00A77preco por unidade\u00A78)" : " \u00A76" + item.sellPrice + " coins"));
        if (item.canBuy()  && item.quantityFree) lore.add("    \u00A76" + item.buyPrice  + " coins \u00A77por item");
        if (item.canSell() && item.quantityFree) lore.add("    \u00A76" + item.sellPrice + " coins \u00A77por item");
        if (item.quantityFree) {
            lore.add("");
            lore.add("\u00A7b» \u00A77Quantidade livre \u00A78(\u00A7fall\u00A78/\u00A7ftodos\u00A78/\u00A7ftudo\u00A78/\u00A7fmax \u00A78= maximo)");
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
