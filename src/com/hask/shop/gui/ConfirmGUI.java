package com.hask.shop.gui;

import com.hask.shop.ShopData;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class ConfirmGUI {

    public static final String TITLE = "\u00A7cConfirmar Compra";

    public static void open(Player p, ShopData shop, int qty) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        ItemStack glass = item(Material.STAINED_GLASS_PANE, (short) 7, "\u00A7r", null);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        int totalItems = shop.amount * qty;
        double totalPrice = shop.price * qty;
        boolean isBuy = shop.type.equals("BUY");

        // Slot 11 — Cancelar
        inv.setItem(11, item(Material.WOOL, (short) 14, "\u00A7c\u00A7lCANCELAR",
            Arrays.asList("\u00A77Clique para cancelar")));

        // Slot 13 — Resumo da transacao
        int displayAmt = Math.min(totalItems, shop.item.getMaxStackSize());
        ItemStack info = new ItemStack(shop.item, displayAmt);
        ItemMeta im = info.getItemMeta();
        im.setDisplayName("\u00A7e\u00A7l" + shop.item.name().replace("_", " "));
        if (isBuy) {
            im.setLore(Arrays.asList(
                "\u00A77Quantidade: \u00A7f" + totalItems + "x",
                "\u00A77Preco unitario: \u00A7f" + shop.price + " coins",
                "",
                "\u00A76Total a pagar: \u00A7f" + totalPrice + " coins"
            ));
        } else {
            im.setLore(Arrays.asList(
                "\u00A77Voce entrega: \u00A7f" + totalItems + "x " + shop.item.name().replace("_", " "),
                "\u00A77Preco unitario: \u00A7f" + shop.price + " coins",
                "",
                "\u00A76Total a receber: \u00A7f" + totalPrice + " coins"
            ));
        }
        info.setItemMeta(im);
        inv.setItem(13, info);

        // Slot 15 — Confirmar
        String confirmLabel = isBuy
            ? "\u00A7a\u00A7lCOMPRAR \u00A7f" + totalItems + "x por \u00A76" + totalPrice + " coins"
            : "\u00A7a\u00A7lVENDER \u00A7f" + totalItems + "x por \u00A76" + totalPrice + " coins";
        inv.setItem(15, item(Material.WOOL, (short) 5, confirmLabel,
            Arrays.asList("\u00A77Clique para confirmar")));

        p.openInventory(inv);
    }

    private static ItemStack item(Material mat, short data, String name, List<String> lore) {
        ItemStack is = new ItemStack(mat, 1, data);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        is.setItemMeta(meta);
        return is;
    }
}
