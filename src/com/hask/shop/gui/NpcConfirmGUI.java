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

    public static final String TITLE_BUY  = "\u00A7dConfirmar Compra";
    public static final String TITLE_SELL = "\u00A7eConfirmar Venda";

    public static void open(Player p, NpcShopItem item, String transactionType, int quantity) {
        boolean isBuy = transactionType.equals("BUY");
        double unitPrice  = isBuy ? item.buyPrice : item.sellPrice;
        int    totalItems = item.quantityFree ? quantity : item.amount * quantity;
        double totalPrice = unitPrice * quantity;

        Inventory inv = Bukkit.createInventory(null, 27, isBuy ? TITLE_BUY : TITLE_SELL);

        ItemStack glass = build(Material.STAINED_GLASS_PANE, (short) 7, "\u00A7r", null);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        // Cancelar
        inv.setItem(11, build(Material.WOOL, (short) 14, "\u00A7c\u00A7lCANCELAR",
            Arrays.asList("\u00A77Clique para cancelar")));

        // Display do item
        inv.setItem(13, buildDisplay(item, transactionType, totalItems, unitPrice, quantity, totalPrice));

        // Confirmar
        String label = isBuy
            ? "\u00A7a\u00A7lCOMPRAR \u00A7fpor \u00A76" + totalPrice + " coins"
            : "\u00A7e\u00A7lVENDER \u00A7fpor \u00A76" + totalPrice + " coins";
        inv.setItem(15, build(Material.WOOL, isBuy ? (short) 5 : (short) 4,
            label, Arrays.asList("\u00A77Clique para confirmar")));

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
            String color  = entry != null ? entry.color  : "\u00A7f";
            short eggData = entry != null ? entry.eggData : 0;
            is = new ItemStack(Material.MONSTER_EGG, Math.min(totalItems, 64), eggData);
            displayName = color + "\u00A7lSpawner de " + ptName;
        } else {
            is = new ItemStack(item.itemType, Math.min(totalItems, 64));
            displayName = "\u00A7f\u00A7l" + NpcShopGUI.formatName(item.itemType.name());
        }

        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(displayName);

        if (quantity > 1) {
            meta.setLore(Arrays.asList(
                isBuy ? "\u00A77Voce vai receber: \u00A7f" + totalItems + "x" : "\u00A77Voce vai entregar: \u00A7f" + totalItems + "x",
                "\u00A77(" + quantity + " unidades de \u00A7f" + item.amount + "x\u00A77)",
                "",
                isBuy ? "\u00A77Voce vai pagar:   \u00A76" + totalPrice + " coins" : "\u00A77Voce vai receber: \u00A76" + totalPrice + " coins",
                "\u00A77(" + quantity + " x \u00A76" + unitPrice + " coins\u00A77)"
            ));
        } else {
            meta.setLore(Arrays.asList(
                isBuy ? "\u00A77Voce vai receber: \u00A7f" + totalItems + "x" : "\u00A77Voce vai entregar: \u00A7f" + totalItems + "x",
                isBuy ? "\u00A77Voce vai pagar:   \u00A76" + totalPrice + " coins" : "\u00A77Voce vai receber: \u00A76" + totalPrice + " coins"
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
