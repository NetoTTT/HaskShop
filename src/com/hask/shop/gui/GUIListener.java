package com.hask.shop.gui;

import com.hask.shop.ConfirmSession;
import com.hask.shop.EditSession;
import com.hask.shop.HaskShop;
import com.hask.shop.ShopData;
import com.hask.shop.SpawnerUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GUIListener implements Listener {
    private final HaskShop plugin;

    public GUIListener(HaskShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        String title = e.getView().getTitle();
        Player p = (Player) e.getWhoClicked();

        // GUI de selecao de mob
        if (title.startsWith(MobSelectGUI.TITLE_PREFIX)) {
            e.setCancelled(true);
            int shopId;
            try {
                shopId = Integer.parseInt(title.substring(MobSelectGUI.TITLE_PREFIX.length()));
            } catch (NumberFormatException ex) { return; }

            ShopData shop = plugin.shopManager.getById(shopId);
            if (shop == null) return;

            int slot = e.getRawSlot();

            if (slot == 49) { // Voltar
                EditGUI.open(p, shop);
                return;
            }

            ItemStack clicked = e.getCurrentItem();
            if (clicked == null || clicked.getType() != Material.MONSTER_EGG) return;
            ItemMeta meta = clicked.getItemMeta();
            if (meta == null || meta.getLore() == null) return;

            // Linha 0 da lore: "§8» §7ZOMBIE" — extrair EntityType name
            String idLine = ChatColor.stripColor(meta.getLore().get(0)).trim(); // "» ZOMBIE"
            String entityTypeName = idLine.replace("»", "").trim();

            shop.spawnerType = entityTypeName;
            plugin.shopManager.save();

            SpawnerUtil.MobEntry entry = SpawnerUtil.getEntry(entityTypeName);
            String ptName = entry != null ? entry.ptName : entityTypeName;
            p.sendMessage("§aMob definido: §f" + ptName + " §8(§7" + entityTypeName + "§8)");
            EditGUI.open(p, shop);
            return;
        }

        // GUI de confirmacao de compra/venda
        if (title.equals(ConfirmGUI.TITLE)) {
            e.setCancelled(true);
            ConfirmSession cs = plugin.pendingConfirm.get(p.getUniqueId());
            if (cs == null) { p.closeInventory(); return; }

            int slot = e.getRawSlot();
            if (slot == 11) {
                plugin.pendingConfirm.remove(p.getUniqueId());
                p.closeInventory();
                p.sendMessage("§7Compra cancelada.");
            } else if (slot == 15) {
                plugin.pendingConfirm.remove(p.getUniqueId());
                p.closeInventory();
                ShopData shop = plugin.shopManager.getById(cs.shopId);
                if (shop == null || !shop.enabled) { p.sendMessage("§cLoja indisponivel."); return; }
                if (shop.type.equals("BUY")) {
                    plugin.shopManager.executeBuy(p, shop, cs.quantity);
                } else {
                    plugin.shopManager.executeSell(p, shop, cs.quantity);
                }
            }
            return;
        }

        // GUI de edicao admin
        if (!title.startsWith(EditGUI.TITLE_PREFIX)) return;
        e.setCancelled(true);

        int shopId;
        try {
            shopId = Integer.parseInt(title.substring(EditGUI.TITLE_PREFIX.length()));
        } catch (NumberFormatException ex) { return; }

        ShopData shop = plugin.shopManager.getById(shopId);
        if (shop == null) return;

        switch (e.getRawSlot()) {
            case 1: // Tipo do mob (apenas para MOB_SPAWNER)
                if (shop.item != Material.MOB_SPAWNER) return;
                MobSelectGUI.open(p, shop);
                break;
            case 4: // Quantidade livre toggle
                shop.askQuantity = !shop.askQuantity;
                plugin.shopManager.save();
                p.sendMessage("§aQuantidade livre: §f" + (shop.askQuantity ? "ATIVADA" : "DESATIVADA"));
                EditGUI.open(p, shop);
                break;
            case 10: // Item
                p.closeInventory();
                p.sendMessage("§eDigite o nome do item em ingles (ex: §fDIAMOND§e, §fMOB_SPAWNER§e):");
                p.sendMessage("§7Digite §ccancel §7para cancelar.");
                plugin.pendingEdit.put(p.getUniqueId(), new EditSession(shopId, "item"));
                break;
            case 12: // Quantidade por unidade
                if (shop.askQuantity) {
                    p.sendMessage("§cDesative a quantidade livre primeiro para editar a unidade base.");
                    return;
                }
                p.closeInventory();
                p.sendMessage("§eDigite a quantidade por unidade §7(1-64)§e:");
                p.sendMessage("§7Digite §ccancel §7para cancelar.");
                plugin.pendingEdit.put(p.getUniqueId(), new EditSession(shopId, "amount"));
                break;
            case 14: // Tipo BUY/SELL
                shop.type = shop.type.equals("BUY") ? "SELL" : "BUY";
                plugin.shopManager.save();
                p.sendMessage("§aModo alterado para: §f" + shop.type);
                EditGUI.open(p, shop);
                break;
            case 16: // Preco
                p.closeInventory();
                p.sendMessage("§eDigite o novo preco:");
                p.sendMessage("§7Digite §ccancel §7para cancelar.");
                plugin.pendingEdit.put(p.getUniqueId(), new EditSession(shopId, "price"));
                break;
            case 22: // Ativar/Desativar
                shop.enabled = !shop.enabled;
                plugin.shopManager.save();
                p.sendMessage(shop.enabled ? "§aLoja §f#" + shopId + " §aativada!" : "§cLoja §f#" + shopId + " §cdesativada!");
                EditGUI.open(p, shop);
                break;
            case 26: // Remover
                plugin.shopManager.remove(shopId);
                p.closeInventory();
                p.sendMessage("§cLoja §f#" + shopId + " §cremovida com sucesso!");
                break;
        }
    }
}
