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

public class EditGUI {

    public static final String TITLE_PREFIX = "§8Loja #";

    public static void open(Player p, ShopData shop) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE_PREFIX + shop.id);

        ItemStack glass = item(Material.STAINED_GLASS_PANE, (short) 7, "§r", null);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        // Slot 10 — Item da loja
        ItemStack itemSlot = new ItemStack(shop.item, Math.max(1, shop.amount));
        ItemMeta im = itemSlot.getItemMeta();
        im.setDisplayName("§e§lItem");
        im.setLore(Arrays.asList(
            "§7Atual: §f" + shop.item.name(),
            "",
            "§7Clique para alterar",
            "§7(digite o nome em inglês)"
        ));
        itemSlot.setItemMeta(im);
        inv.setItem(10, itemSlot);

        // Slot 12 — Quantidade (ou unidade base se quantidade livre)
        if (shop.askQuantity) {
            inv.setItem(12, item(Material.PAPER, (short) 0, "§7§lUNIDADE BASE",
                Arrays.asList(
                    "§7Atual: §f" + shop.amount + "x por unidade",
                    "",
                    "§7Ex: membro digita §f3 §7→ recebe §f" + (shop.amount * 3) + "x",
                    "§7Clique para alterar"
                )));
        } else {
            inv.setItem(12, item(Material.PAPER, (short) 0, "§b§lQuantidade por clique",
                Arrays.asList("§7Atual: §f" + shop.amount + "x", "", "§7Clique para alterar")));
        }

        // Slot 14 — Tipo BUY / SELL
        boolean isBuy = shop.type.equals("BUY");
        inv.setItem(14, item(
            isBuy ? Material.EMERALD : Material.REDSTONE,
            (short) 0,
            isBuy ? "§a§lMODO: COMPRA" : "§c§lMODO: VENDA",
            Arrays.asList(
                isBuy ? "§7Jogador §apaga §7e §arecebe §7o item" : "§7Jogador §centrega §7o item e §crecebe §7coins",
                "",
                "§7Clique para alternar"
            )
        ));

        // Slot 16 — Preço
        inv.setItem(16, item(Material.GOLD_NUGGET, (short) 0, "§6§lPreço",
            Arrays.asList("§7Atual: §f" + shop.price + " coins", "", "§7Clique para alterar")));

        // Slot 1 — Tipo do mob (apenas quando item e MOB_SPAWNER)
        if (shop.item == org.bukkit.Material.MOB_SPAWNER) {
            String mobAtual = shop.spawnerType != null ? shop.spawnerType : "§cNAO DEFINIDO";
            inv.setItem(1, item(Material.MOB_SPAWNER, (short) 0, "§d§lTIPO DO MOB",
                Arrays.asList(
                    "§7Atual: §f" + mobAtual,
                    "",
                    "§7Clique para alterar",
                    "§7Ex: §fZOMBIE§7, §fSKELETON§7, §fBLAZE§7, §fSPIDER"
                )));
        }

        // Slot 4 — Quantidade livre
        inv.setItem(4, item(
            Material.WOOL,
            shop.askQuantity ? (short) 5 : (short) 14,
            shop.askQuantity ? "§a§lQUANTIDADE LIVRE" : "§c§lQUANTIDADE FIXA",
            Arrays.asList(
                shop.askQuantity ? "§7Membro escolhe a qtd ao clicar" : "§7Vende sempre §f" + shop.amount + "x §7por clique",
                "",
                "§7Clique para alternar"
            )
        ));

        // Slot 22 — Ativar / Desativar
        inv.setItem(22, item(
            shop.enabled ? Material.WOOL : Material.WOOL,
            shop.enabled ? (short) 5 : (short) 14,
            shop.enabled ? "§a§lLOJA ATIVA" : "§c§lLOJA DESATIVADA",
            Arrays.asList("§7Clique para " + (shop.enabled ? "§cdesativar" : "§aativar"))
        ));

        // Slot 26 — Remover
        inv.setItem(26, item(Material.BARRIER, (short) 0, "§c§lREMOVER LOJA",
            Arrays.asList("§7Remove esta loja permanentemente.", "§cClique para confirmar.")));

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
