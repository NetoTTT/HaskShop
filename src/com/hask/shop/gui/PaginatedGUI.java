package com.hask.shop.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class PaginatedGUI {

    public static final String TITLE_PREFIX = "\u00A78Pag. ";
    public static final int[] SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43
    };
    public static final int ITEMS_PER_PAGE = 28;

    private final String title;
    private final String subtitle;
    private final List<ItemStack> items;
    final ClickHandler handler;
    final ClickHandlerEvent eventHandler;

    public PaginatedGUI(String title, String subtitle, List<ItemStack> items, ClickHandler handler) {
        this.title = title;
        this.subtitle = subtitle;
        this.items = items;
        this.handler = handler;
        this.eventHandler = null;
    }

    public PaginatedGUI(String title, String subtitle, List<ItemStack> items, ClickHandlerEvent eventHandler) {
        this.title = title;
        this.subtitle = subtitle;
        this.items = items;
        this.handler = null;
        this.eventHandler = eventHandler;
    }

    public void open(Player player) {
        open(player, 0);
    }

    public void open(Player player, int page) {
        int totalPages = Math.max(1, (int) Math.ceil((double) items.size() / ITEMS_PER_PAGE));
        if (page < 0) page = 0;
        if (page >= totalPages) page = totalPages - 1;

        Inventory inv = Bukkit.createInventory(new PaginatedHolder(this, page), 54, TITLE_PREFIX + (page + 1) + " \u00A78- \u00A7f\u00A7l" + title);

        ItemStack glass = make(Material.STAINED_GLASS_PANE, (short) 7, "\u00A7r", null);
        for (int i = 0; i < 54; i++) inv.setItem(i, glass);

        inv.setItem(4, make(Material.NETHER_STAR, (short) 0, "\u00A76" + title,
            Arrays.asList(
                "\u00A77" + subtitle,
                "\u00A77Pagina \u00A7f" + (page + 1) + " \u00A77de \u00A7f" + totalPages
            )));

        int start = page * ITEMS_PER_PAGE;
        for (int i = 0; i < SLOTS.length; i++) {
            int idx = start + i;
            if (idx >= items.size()) break;
            inv.setItem(SLOTS[i], items.get(idx));
        }

        if (page > 0) {
            inv.setItem(45, make(Material.ARROW, (short) 0, "\u00A7a« Anterior",
                Arrays.asList("\u00A77Pagina " + page)));
        }
        if (page < totalPages - 1) {
            inv.setItem(53, make(Material.ARROW, (short) 0, "\u00A7aPróximo »",
                Arrays.asList("\u00A77Pagina " + (page + 2))));
        }

        inv.setItem(49, make(Material.BARRIER, (short) 0, "\u00A7cFechar", null));

        player.openInventory(inv);
    }
    public List<ItemStack> getItems() { return items; }

    public static ItemStack make(Material mat, short data, String name, List<String> lore) {
        ItemStack is = new ItemStack(mat, 1, data);
        ItemMeta meta = is.getItemMeta();
        if (name != null) meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        is.setItemMeta(meta);
        return is;
    }

    public interface ClickHandler {
        void onClick(Player player, int slotIndex, int realIndex, ItemStack item);
    }

    public interface ClickHandlerEvent {
        void onClick(Player player, int slotIndex, int realIndex, ItemStack item, org.bukkit.event.inventory.InventoryClickEvent event);
    }

}
