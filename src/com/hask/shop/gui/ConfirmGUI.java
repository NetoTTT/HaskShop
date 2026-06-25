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

    public static final String TITLE = "§cConfirmar Compra";

    public static void open(Player p, ShopData shop, int qty) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);

        ItemStack glass = item(Material.STAINED_GLASS_PANE, (short) 7, "§r", null);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        int totalItems = shop.amount * qty;
        double totalPrice = shop.price * qty;
        boolean isBuy = shop.type.equals("BUY");

        // Slot 11 — Cancelar
        inv.setItem(11, item(Material.WOOL, (short) 14, "§c§lCANCELAR",
            Arrays.asList("§7Clique para cancelar")));

        // Slot 13 — Resumo da transacao
        int displayAmt = Math.min(totalItems, shop.item.getMaxStackSize());
        ItemStack info = new ItemStack(shop.item, displayAmt);
        ItemMeta im = info.getItemMeta();
        im.setDisplayName("§e§l" + shop.item.name().replace("_", " "));
        if (isBuy) {
            im.setLore(Arrays.asList(
                "§7Quantidade: §f" + totalItems + "x",
                "§7Preco unitario: §f" + shop.price + " coins",
                "",
                "§6Total a pagar: §f" + totalPrice + " coins"
            ));
        } else {
            im.setLore(Arrays.asList(
                "§7Voce entrega: §f" + totalItems + "x " + shop.item.name().replace("_", " "),
                "§7Preco unitario: §f" + shop.price + " coins",
                "",
                "§6Total a receber: §f" + totalPrice + " coins"
            ));
        }
        info.setItemMeta(im);
        inv.setItem(13, info);

        // Slot 15 — Confirmar
        String confirmLabel = isBuy
            ? "§a§lCOMPRAR §f" + totalItems + "x por §6" + totalPrice + " coins"
            : "§a§lVENDER §f" + totalItems + "x por §6" + totalPrice + " coins";
        inv.setItem(15, item(Material.WOOL, (short) 5, confirmLabel,
            Arrays.asList("§7Clique para confirmar")));

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
