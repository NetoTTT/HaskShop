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

    public static final String TITLE_PREFIX = "\u00A78Loja #";

    public static void open(Player p, ShopData shop) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE_PREFIX + shop.id);

        ItemStack glass = item(Material.STAINED_GLASS_PANE, (short) 7, "\u00A7r", null);
        for (int i = 0; i < 27; i++) inv.setItem(i, glass);

        // Slot 10 — Item da loja
        ItemStack itemSlot = new ItemStack(shop.item, Math.max(1, shop.amount));
        ItemMeta im = itemSlot.getItemMeta();
        im.setDisplayName("\u00A7e\u00A7lItem");
        im.setLore(Arrays.asList(
            "\u00A77Atual: \u00A7f" + shop.item.name(),
            "",
            "\u00A77Clique para alterar",
            "\u00A77(digite o nome em inglês)"
        ));
        itemSlot.setItemMeta(im);
        inv.setItem(10, itemSlot);

        // Slot 12 — Quantidade (ou unidade base se quantidade livre)
        if (shop.askQuantity) {
            inv.setItem(12, item(Material.PAPER, (short) 0, "\u00A77\u00A7lUNIDADE BASE",
                Arrays.asList(
                    "\u00A77Atual: \u00A7f" + shop.amount + "x por unidade",
                    "",
                    "\u00A77Ex: membro digita \u00A7f3 \u00A77→ recebe \u00A7f" + (shop.amount * 3) + "x",
                    "\u00A77Clique para alterar"
                )));
        } else {
            inv.setItem(12, item(Material.PAPER, (short) 0, "\u00A7b\u00A7lQuantidade por clique",
                Arrays.asList("\u00A77Atual: \u00A7f" + shop.amount + "x", "", "\u00A77Clique para alterar")));
        }

        // Slot 14 — Tipo BUY / SELL
        boolean isBuy = shop.type.equals("BUY");
        inv.setItem(14, item(
            isBuy ? Material.EMERALD : Material.REDSTONE,
            (short) 0,
            isBuy ? "\u00A7a\u00A7lMODO: COMPRA" : "\u00A7c\u00A7lMODO: VENDA",
            Arrays.asList(
                isBuy ? "\u00A77Jogador \u00A7apaga \u00A77e \u00A7arecebe \u00A77o item" : "\u00A77Jogador \u00A7centrega \u00A77o item e \u00A7crecebe \u00A77coins",
                "",
                "\u00A77Clique para alternar"
            )
        ));

        // Slot 16 — Preço
        inv.setItem(16, item(Material.GOLD_NUGGET, (short) 0, "\u00A76\u00A7lPreço",
            Arrays.asList("\u00A77Atual: \u00A7f" + shop.price + " coins", "", "\u00A77Clique para alterar")));

        // Slot 1 — Tipo do mob (apenas quando item e MOB_SPAWNER)
        if (shop.item == org.bukkit.Material.MOB_SPAWNER) {
            String mobAtual = shop.spawnerType != null ? shop.spawnerType : "\u00A7cNAO DEFINIDO";
            inv.setItem(1, item(Material.MOB_SPAWNER, (short) 0, "\u00A7d\u00A7lTIPO DO MOB",
                Arrays.asList(
                    "\u00A77Atual: \u00A7f" + mobAtual,
                    "",
                    "\u00A77Clique para alterar",
                    "\u00A77Ex: \u00A7fZOMBIE\u00A77, \u00A7fSKELETON\u00A77, \u00A7fBLAZE\u00A77, \u00A7fSPIDER"
                )));
        }

        // Slot 4 — Quantidade livre
        inv.setItem(4, item(
            Material.WOOL,
            shop.askQuantity ? (short) 5 : (short) 14,
            shop.askQuantity ? "\u00A7a\u00A7lQUANTIDADE LIVRE" : "\u00A7c\u00A7lQUANTIDADE FIXA",
            Arrays.asList(
                shop.askQuantity ? "\u00A77Membro escolhe a qtd ao clicar" : "\u00A77Vende sempre \u00A7f" + shop.amount + "x \u00A77por clique",
                "",
                "\u00A77Clique para alternar"
            )
        ));

        // Slot 6 — Item custom (NBT)
        String customAtual = shop.customItemId != null ? shop.customItemId : "\u00A7cNENHUM";
        inv.setItem(6, item(Material.ANVIL, (short) 0, "\u00A75\u00A7lITEM CUSTOM (NBT)",
            Arrays.asList(
                "\u00A77Atual: \u00A7f" + customAtual,
                "",
                "\u00A77Clique para alterar",
                "\u00A77Ex: \u00A7fgerador_bedrock\u00A77, \u00A7fescavador_chunk",
                "\u00A77Digite \u00A7fnone \u00A77para remover"
            )));

        // Slot 22 — Ativar / Desativar
        inv.setItem(22, item(
            shop.enabled ? Material.WOOL : Material.WOOL,
            shop.enabled ? (short) 5 : (short) 14,
            shop.enabled ? "\u00A7a\u00A7lLOJA ATIVA" : "\u00A7c\u00A7lLOJA DESATIVADA",
            Arrays.asList("\u00A77Clique para " + (shop.enabled ? "\u00A7cdesativar" : "\u00A7aativar"))
        ));

        // Slot 26 — Remover
        inv.setItem(26, item(Material.BARRIER, (short) 0, "\u00A7c\u00A7lREMOVER LOJA",
            Arrays.asList("\u00A77Remove esta loja permanentemente.", "\u00A7cClique para confirmar.")));

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
