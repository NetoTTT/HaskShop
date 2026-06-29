package com.hask.shop.shop;

import com.hask.cash.api.HaskCashAPI;
import com.hask.shop.HaskShop;
import com.hask.shop.gui.PaginatedGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class CashShopGUI {

    private static final DecimalFormat FMT = new DecimalFormat("#,##0.00");

    public static void open(Player player) {
        HaskShop plugin = HaskShop.instance;
        java.util.List<ItemStack> displayItems = new ArrayList<>();

        for (CashShopItem shopItem : plugin.cashShopManager.getItems()) {
            displayItems.add(buildDisplayItem(shopItem));
        }

        PaginatedGUI gui = new PaginatedGUI(
            "Loja Cash",
            "Clique para comprar com cash",
            displayItems,
            (p, slotIndex, realIndex, item) -> {
                CashShopItem selected = plugin.cashShopManager.getItem(realIndex);
                if (selected == null) return;

                ItemStack template = plugin.customItemRegistry.get(selected.customItemId);
                if (template == null) {
                    p.sendMessage("\u00A7cItem \u00A7f" + selected.customItemId + " \u00A7cindisponivel.");
                    return;
                }

                double bal = HaskCashAPI.getCash(p);
                if (bal < selected.price) {
                    p.sendMessage("\u00A7cCash insuficiente! Voce tem \u00A7f" + FMT.format(bal) + " \u00A7cprecisa de \u00A7f" + FMT.format(selected.price));
                    return;
                }

                int space = plugin.shopManager.freeSpace(p, template.getType());
                if (space < selected.amount) {
                    p.sendMessage("\u00A7cInventario cheio!");
                    return;
                }

                HaskCashAPI.removeCash(p, selected.price);

                ItemStack toGive = template.clone();
                toGive.setAmount(selected.amount);
                p.getInventory().addItem(toGive);
                p.sendMessage("\u00A7aComprado! \u00A7f" + selected.amount + "x \u00A7a" + ChatColor.stripColor(item.getItemMeta().getDisplayName()) + " \u00A7apor \u00A76" + FMT.format(selected.price) + " cash");
                p.closeInventory();
            }
        );

        gui.open(player);
    }

    private static ItemStack buildDisplayItem(CashShopItem shopItem) {
        HaskShop plugin = HaskShop.instance;
        ItemStack template = plugin.customItemRegistry.get(shopItem.customItemId);
        if (template == null) {
            return PaginatedGUI.make(Material.BARRIER, (short) 0, "\u00A7c" + shopItem.id, null);
        }

        ItemStack display = template.clone();
        display.setAmount(Math.min(shopItem.amount, 64));

        ItemMeta meta = display.getItemMeta();
        String name = meta.hasDisplayName() ? meta.getDisplayName() : "\u00A7f" + shopItem.id;
        meta.setDisplayName("\u00A76" + ChatColor.stripColor(name));

        List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
        lore.add("");
        lore.add("\u00A76Preco: \u00A7f" + FMT.format(shopItem.price) + " cash");
        lore.add("\u00A77Quantidade: \u00A7f" + shopItem.amount + "x");
        lore.add("");
        lore.add("\u00A7eClique para comprar!");
        meta.setLore(lore);
        display.setItemMeta(meta);

        return display;
    }

}
