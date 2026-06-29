package com.hask.shop.gui;

import com.hask.shop.HaskShop;
import org.bukkit.ChatColor;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustomItemsGUI {

    public static void open(Player player) {
        HaskShop plugin = HaskShop.instance;
        Map<String, ItemStack> allItems = plugin.customItemRegistry.getAll();

        List<ItemStack> displayItems = new ArrayList<>();

        for (Map.Entry<String, ItemStack> entry : allItems.entrySet()) {
            String id = entry.getKey();
            ItemStack template = entry.getValue();

            ItemStack display = template.clone();
            display.setAmount(1);

            ItemMeta meta = display.getItemMeta();
            String name = meta.hasDisplayName() ? meta.getDisplayName() : "\u00A7f" + id;
            meta.setDisplayName("\u00A76" + ChatColor.stripColor(name));

            List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
            lore.add("");
            lore.add("\u00A78ID: \u00A7f" + id);
            lore.add("\u00A78Tipo: \u00A7f" + template.getType().name());
            lore.add("");
            lore.add("\u00A7eClique para pegar 1x");
            lore.add("\u00A7eShift+Clique para pegar 64x");
            meta.setLore(lore);
            display.setItemMeta(meta);

            displayItems.add(display);
        }

        PaginatedGUI gui = new PaginatedGUI(
            "Itens Customizados",
            allItems.size() + " item(ns) registrados",
            displayItems,
            (PaginatedGUI.ClickHandlerEvent) (p, slotIndex, realIndex, item, event) -> {
                String raid = getItemIdFromLore(item);
                if (raid == null) return;

                ItemStack template = plugin.customItemRegistry.get(raid);
                if (template == null) return;

                boolean shift = event != null && (event.isShiftClick() || event.getClick() == org.bukkit.event.inventory.ClickType.SHIFT_LEFT || event.getClick() == org.bukkit.event.inventory.ClickType.SHIFT_RIGHT);

                int amount = shift ? 64 : 1;
                ItemStack toGive = template.clone();
                toGive.setAmount(amount);
                p.getInventory().addItem(toGive);
                p.sendMessage("\u00A7aItem \u00A7f" + raid + " \u00A7aadicionado (\u00A7f" + amount + "x\u00A7a)!");
            }
        );

        gui.open(player);
    }

    private static String getItemIdFromLore(ItemStack item) {
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) return null;
        for (String line : item.getItemMeta().getLore()) {
            if (line.startsWith("\u00A78ID: \u00A7f")) {
                return ChatColor.stripColor(line.substring(6)).trim();
            }
        }
        return null;
    }

}
