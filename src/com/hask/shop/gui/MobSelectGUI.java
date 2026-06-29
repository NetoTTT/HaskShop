package com.hask.shop.gui;

import com.hask.shop.ShopData;
import com.hask.shop.SpawnerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class MobSelectGUI {

    public static final String TITLE_PREFIX = "\u00A75Spawner \u00A78#";

    // Slots internos: 4 linhas de 7 + 2 extras na linha de navegacao
    private static final int[] INNER_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25,
        28, 29, 30, 31, 32, 33, 34,
        37, 38, 39, 40, 41, 42, 43,
        46, 47
    };

    public static void open(Player p, ShopData shop) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE_PREFIX + shop.id);

        ItemStack glass = slot(Material.STAINED_GLASS_PANE, (short) 7, "\u00A7r", null);
        for (int i = 0; i < 54; i++) inv.setItem(i, glass);

        // Slot de titulo
        String atualDisplay = shop.spawnerType != null
            ? (SpawnerUtil.getEntry(shop.spawnerType) != null
                ? SpawnerUtil.getEntry(shop.spawnerType).ptName
                : shop.spawnerType)
            : "Nenhum";
        inv.setItem(4, slot(Material.MOB_SPAWNER, (short) 0, "\u00A75\u00A7lSELECIONAR MOB",
            Arrays.asList(
                "\u00A77Atual: \u00A7f" + atualDisplay,
                "",
                "\u00A7cHostil   \u00A7eNeutro   \u00A7aPassivo"
            )));

        // Mobs
        List<SpawnerUtil.MobEntry> mobs = SpawnerUtil.MOBS;
        for (int i = 0; i < Math.min(mobs.size(), INNER_SLOTS.length); i++) {
            SpawnerUtil.MobEntry mob = mobs.get(i);
            boolean selected = mob.type.name().equals(shop.spawnerType);
            ItemStack egg = new ItemStack(Material.MONSTER_EGG, 1, mob.eggData);
            ItemMeta meta = egg.getItemMeta();
            meta.setDisplayName(mob.color + "\u00A7l" + mob.ptName);
            meta.setLore(Arrays.asList(
                "\u00A78» \u00A77" + mob.type.name(),
                "",
                selected ? "\u00A7a\u00A7l✔ Selecionado" : "\u00A77Clique para selecionar"
            ));
            egg.setItemMeta(meta);
            inv.setItem(INNER_SLOTS[i], egg);
        }

        // Botao voltar
        inv.setItem(49, slot(Material.ARROW, (short) 0, "\u00A77Voltar",
            Arrays.asList("\u00A77Volta para a edicao da loja")));

        p.openInventory(inv);
    }

    private static ItemStack slot(Material mat, short data, String name, List<String> lore) {
        ItemStack is = new ItemStack(mat, 1, data);
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        is.setItemMeta(meta);
        return is;
    }
}
