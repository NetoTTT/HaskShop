package com.hask.shop.gui;

import com.hask.shop.NpcShopItem;
import com.hask.shop.SpawnerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class NpcConfirmGUI {

    public static final String TITLE_BUY  = "§dConfirmar Compra";
    public static final String TITLE_SELL = "§eConfirmar Venda";

    public static void open(Player p, NpcShopItem item, String transactionType, int quantity) {
        boolean isBuy = transactionType.equals("BUY");
        double unitPrice  = isBuy ? item.buyPrice : item.sellPrice;
        int    totalItems = item.quantityFree ? quantity : item.amount * quantity;
        double totalPrice = unitPrice * quantity;

        Inventory inv = Bukkit.createInventory(null, 27, isBuy ? TITLE_BUY : TITLE_SELL);

        ItemStack glass = build(Material.STAINED_GLASS_PANE, (short) 7, "§r", null);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        // Cancelar
        inv.setItem(11, build(Material.WOOL, (short) 14, "§c§lCANCELAR",
            Arrays.asList("§7Clique para cancelar")));

        // Display do item
        inv.setItem(13, buildDisplay(item, transactionType, totalItems, unitPrice, quantity, totalPrice));

        // Confirmar
        String label = isBuy
            ? "§a§lCOMPRAR §fpor §6" + totalPrice + " coins"
            : "§e§lVENDER §fpor §6" + totalPrice + " coins";
        inv.setItem(15, build(Material.WOOL, isBuy ? (short) 5 : (short) 4,
            label, Arrays.asList("§7Clique para confirmar")));

        p.openInventory(inv);
    }

    private static ItemStack buildDisplay(NpcShopItem item, String transactionType,
                                          int totalItems, double unitPrice, int quantity, double totalPrice) {
        boolean isBuy = transactionType.equals("BUY");
        ItemStack is;
        String displayName;

        if (item.isSpawner()) {
            SpawnerUtil.MobEntry entry = SpawnerUtil.getEntry(item.mobType);
            String ptName = entry != null ? entry.ptName : item.mobType;
            String color  = entry != null ? entry.color  : "§f";
            short eggData = entry != null ? entry.eggData : 0;
            is = new ItemStack(Material.MONSTER_EGG, Math.min(totalItems, 64), eggData);
            displayName = color + "§lSpawner de " + ptName;
        } else {
            is = new ItemStack(item.itemType, Math.min(totalItems, 64));
            displayName = "§f§l" + NpcShopGUI.formatName(item.itemType.name());
        }

        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(displayName);

        if (quantity > 1) {
            meta.setLore(Arrays.asList(
                isBuy ? "§7Voce vai receber: §f" + totalItems + "x" : "§7Voce vai entregar: §f" + totalItems + "x",
                "§7(" + quantity + " unidades de §f" + item.amount + "x§7)",
                "",
                isBuy ? "§7Voce vai pagar:   §6" + totalPrice + " coins" : "§7Voce vai receber: §6" + totalPrice + " coins",
                "§7(" + quantity + " x §6" + unitPrice + " coins§7)"
            ));
        } else {
            meta.setLore(Arrays.asList(
                isBuy ? "§7Voce vai receber: §f" + totalItems + "x" : "§7Voce vai entregar: §f" + totalItems + "x",
                isBuy ? "§7Voce vai pagar:   §6" + totalPrice + " coins" : "§7Voce vai receber: §6" + totalPrice + " coins"
            ));
        }
        is.setItemMeta(meta);
        return is;
    }

    private static ItemStack build(Material mat, short data, String name, List<String> lore) {
        ItemStack is = new ItemStack(mat, 1, data);
        ItemMeta m = is.getItemMeta();
        m.setDisplayName(name);
        if (lore != null) m.setLore(lore);
        is.setItemMeta(m);
        return is;
    }
}
