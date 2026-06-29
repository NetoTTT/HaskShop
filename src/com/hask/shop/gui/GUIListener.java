package com.hask.shop.gui;

import com.hask.shop.ConfirmSession;
import com.hask.shop.EditSession;
import com.hask.shop.HaskShop;
import com.hask.shop.NpcConfirmSession;
import com.hask.shop.NpcQtySession;
import com.hask.shop.NpcShopItem;
import com.hask.shop.NpcShopManager;
import com.hask.shop.ShopData;
import com.hask.shop.SpawnerUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
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

        // GUI de catalogo NPC
        if (title.startsWith(NpcShopGUI.TITLE_PREFIX)) {
            e.setCancelled(true);
            String shopId = title.substring(NpcShopGUI.TITLE_PREFIX.length());
            NpcShopManager.NpcShop shop = plugin.npcShopManager.getShop(shopId);
            if (shop == null) return;

            int slot = e.getRawSlot();
            int page = plugin.npcShopPage.getOrDefault(p.getUniqueId(), 0);
            int totalPages = Math.max(1, (int) Math.ceil((double) shop.items.size() / NpcShopGUI.ITEMS_PER_PAGE));

            if (slot == 49) { p.closeInventory(); return; } // Fechar

            if (slot == 45 && page > 0) { // Anterior
                plugin.getServer().getScheduler().runTask(plugin, () -> NpcShopGUI.open(p, shop, page - 1));
                return;
            }
            if (slot == 53 && page < totalPages - 1) { // Proximo
                plugin.getServer().getScheduler().runTask(plugin, () -> NpcShopGUI.open(p, shop, page + 1));
                return;
            }

            // Verificar se clicou num slot de item
            int slotIndex = -1;
            for (int i = 0; i < NpcShopGUI.INNER_SLOTS.length; i++) {
                if (NpcShopGUI.INNER_SLOTS[i] == slot) { slotIndex = i; break; }
            }
            if (slotIndex == -1) return;

            int globalIndex = page * NpcShopGUI.ITEMS_PER_PAGE + slotIndex;
            if (globalIndex >= shop.items.size()) return;

            NpcShopItem shopItem = shop.items.get(globalIndex);

            // Determinar BUY ou SELL pelo tipo de clique
            String txType;
            ClickType click = e.getClick();
            if (click == ClickType.LEFT || click == ClickType.SHIFT_LEFT) {
                if (!shopItem.canBuy()) { p.sendMessage("\u00A7cEste item nao esta disponivel para compra."); return; }
                txType = "BUY";
            } else if (click == ClickType.RIGHT || click == ClickType.SHIFT_RIGHT) {
                if (!shopItem.canSell()) { p.sendMessage("\u00A7cEste item nao esta disponivel para venda."); return; }
                txType = "SELL";
            } else {
                return;
            }

            if (shopItem.quantityFree) {
                plugin.pendingNpcQty.put(p.getUniqueId(), new NpcQtySession(shopId, shopItem, txType));
                final String finalTxType = txType;
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    p.closeInventory();
                    p.sendMessage("\u00A7eDigite a quantidade de unidades \u00A77(1 unidade = \u00A7f" + shopItem.amount + "x\u00A77)\u00A7e:");
                    p.sendMessage("\u00A77Digite \u00A7ccancel \u00A77para cancelar.");
                });
            } else {
                final String finalTxType = txType;
                plugin.pendingNpcConfirm.put(p.getUniqueId(), new NpcConfirmSession(shopId, shopItem, txType, 1));
                plugin.getServer().getScheduler().runTask(plugin, () -> NpcConfirmGUI.open(p, shopItem, finalTxType, 1));
            }
            return;
        }

        // GUI paginada generica (SkillSelect, etc)
        if (title.startsWith(PaginatedGUI.TITLE_PREFIX)) {
            e.setCancelled(true);
            if (!(e.getInventory().getHolder() instanceof PaginatedHolder)) return;
            PaginatedHolder holder = (PaginatedHolder) e.getInventory().getHolder();
            int slot = e.getRawSlot();
            int page = holder.getPage();
            int totalPages = Math.max(1, (int) Math.ceil((double) holder.getGui().getItems().size() / PaginatedGUI.ITEMS_PER_PAGE));

            if (slot == 49) { p.closeInventory(); return; }
            if (slot == 45 && page > 0) {
                holder.getGui().open(p, page - 1);
                return;
            }
            if (slot == 53 && page < totalPages - 1) {
                holder.getGui().open(p, page + 1);
                return;
            }

            int slotIndex = -1;
            for (int i = 0; i < PaginatedGUI.SLOTS.length; i++) {
                if (PaginatedGUI.SLOTS[i] == slot) { slotIndex = i; break; }
            }
            if (slotIndex == -1) return;

            int realIndex = page * PaginatedGUI.ITEMS_PER_PAGE + slotIndex;
            if (realIndex >= holder.getGui().getItems().size()) return;

            Object h = holder.getGui().handler != null ? holder.getGui().handler : holder.getGui().eventHandler;
            ItemStack clicked = holder.getGui().getItems().get(realIndex);
            if (h instanceof PaginatedGUI.ClickHandlerEvent) {
                ((PaginatedGUI.ClickHandlerEvent) h).onClick(p, slotIndex, realIndex, clicked, e);
            } else if (h instanceof PaginatedGUI.ClickHandler) {
                ((PaginatedGUI.ClickHandler) h).onClick(p, slotIndex, realIndex, clicked);
            }
            return;
        }

        // GUI de confirmacao NPC (BUY ou SELL)
        if (title.equals(NpcConfirmGUI.TITLE_BUY) || title.equals(NpcConfirmGUI.TITLE_SELL)) {
            e.setCancelled(true);
            NpcConfirmSession cs = plugin.pendingNpcConfirm.get(p.getUniqueId());
            if (cs == null) { p.closeInventory(); return; }

            int slot = e.getRawSlot();
            if (slot == 11) {
                plugin.pendingNpcConfirm.remove(p.getUniqueId());
                p.closeInventory();
                p.sendMessage("\u00A77Operacao cancelada.");
            } else if (slot == 15) {
                plugin.pendingNpcConfirm.remove(p.getUniqueId());
                p.closeInventory();
                executeNpcTransaction(p, cs);
            }
            return;
        }

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

            // Linha 0 da lore: "\u00A78» \u00A77ZOMBIE" — extrair EntityType name
            String idLine = ChatColor.stripColor(meta.getLore().get(0)).trim(); // "» ZOMBIE"
            String entityTypeName = idLine.replace("»", "").trim();

            shop.spawnerType = entityTypeName;
            plugin.shopManager.save();

            SpawnerUtil.MobEntry entry = SpawnerUtil.getEntry(entityTypeName);
            String ptName = entry != null ? entry.ptName : entityTypeName;
            p.sendMessage("\u00A7aMob definido: \u00A7f" + ptName + " \u00A78(\u00A77" + entityTypeName + "\u00A78)");
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
                p.sendMessage("\u00A77Compra cancelada.");
            } else if (slot == 15) {
                plugin.pendingConfirm.remove(p.getUniqueId());
                p.closeInventory();
                ShopData shop = plugin.shopManager.getById(cs.shopId);
                if (shop == null || !shop.enabled) { p.sendMessage("\u00A7cLoja indisponivel."); return; }
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
                p.sendMessage("\u00A7aQuantidade livre: \u00A7f" + (shop.askQuantity ? "ATIVADA" : "DESATIVADA"));
                EditGUI.open(p, shop);
                break;
            case 6: // Item custom (NBT)
                p.closeInventory();
                p.sendMessage("\u00A7eDigite o ID do item custom \u00A77(ex: \u00A7fgerador_bedrock\u00A77, \u00A7fescavador_chunk\u00A77, \u00A7fnone\u00A77 para remover)\u00A7e:");
                p.sendMessage("\u00A77Use \u00A7f/hs items \u00A77para listar os disponiveis.");
                p.sendMessage("\u00A77Digite \u00A7ccancel \u00A77para cancelar.");
                plugin.pendingEdit.put(p.getUniqueId(), new EditSession(shopId, "customitem"));
                break;
            case 10: // Item
                p.closeInventory();
                p.sendMessage("\u00A7eDigite o nome do item em ingles (ex: \u00A7fDIAMOND\u00A7e, \u00A7fMOB_SPAWNER\u00A7e):");
                p.sendMessage("\u00A77Digite \u00A7ccancel \u00A77para cancelar.");
                plugin.pendingEdit.put(p.getUniqueId(), new EditSession(shopId, "item"));
                break;
            case 12: // Quantidade por unidade
                if (shop.askQuantity) {
                    p.sendMessage("\u00A7cDesative a quantidade livre primeiro para editar a unidade base.");
                    return;
                }
                p.closeInventory();
                p.sendMessage("\u00A7eDigite a quantidade por unidade \u00A77(1-64)\u00A7e:");
                p.sendMessage("\u00A77Digite \u00A7ccancel \u00A77para cancelar.");
                plugin.pendingEdit.put(p.getUniqueId(), new EditSession(shopId, "amount"));
                break;
            case 14: // Tipo BUY/SELL
                shop.type = shop.type.equals("BUY") ? "SELL" : "BUY";
                plugin.shopManager.save();
                p.sendMessage("\u00A7aModo alterado para: \u00A7f" + shop.type);
                EditGUI.open(p, shop);
                break;
            case 16: // Preco
                p.closeInventory();
                p.sendMessage("\u00A7eDigite o novo preco:");
                p.sendMessage("\u00A77Digite \u00A7ccancel \u00A77para cancelar.");
                plugin.pendingEdit.put(p.getUniqueId(), new EditSession(shopId, "price"));
                break;
            case 22: // Ativar/Desativar
                shop.enabled = !shop.enabled;
                plugin.shopManager.save();
                p.sendMessage(shop.enabled ? "\u00A7aLoja \u00A7f#" + shopId + " \u00A7aativada!" : "\u00A7cLoja \u00A7f#" + shopId + " \u00A7cdesativada!");
                EditGUI.open(p, shop);
                break;
            case 26: // Remover
                plugin.shopManager.remove(shopId);
                p.closeInventory();
                p.sendMessage("\u00A7cLoja \u00A7f#" + shopId + " \u00A7cremovida com sucesso!");
                break;
        }
    }

    private void executeNpcTransaction(Player p, NpcConfirmSession cs) {
        NpcShopItem item = cs.item;
        int quantity   = cs.quantity;
        // quantity_free: player digitou direto o total de itens; senao: amount * unidades
        int totalItems = item.quantityFree ? quantity : item.amount * quantity;

        if (cs.transactionType.equals("BUY")) {
            double totalPrice = item.buyPrice * quantity;

            int space = plugin.shopManager.freeSpace(p, item.itemType);
            if (space < totalItems) {
                p.sendMessage("\u00A7cInventario sem espaco para \u00A7f" + totalItems + "x \u00A7citens!");
                return;
            }
            if (HaskShop.economy.getBalance(p.getName()) < totalPrice) {
                p.sendMessage("\u00A7cCoins insuficientes! Necessario: \u00A76" + totalPrice + " coins\u00A7c.");
                return;
            }
            HaskShop.economy.withdrawPlayer(p.getName(), totalPrice);

            if (item.isSpawner()) {
                for (int i = 0; i < quantity; i++) {
                    p.getInventory().addItem(SpawnerUtil.createOne(item.mobType, item.amount));
                }
                SpawnerUtil.MobEntry entry = SpawnerUtil.getEntry(item.mobType);
                String name = entry != null ? entry.ptName : item.mobType;
                p.sendMessage("\u00A7aComprado! \u00A7f" + quantity + "x Spawner de " + name + " \u00A7apor \u00A76" + totalPrice + " coins\u00A7a.");
            } else {
                for (org.bukkit.inventory.ItemStack stack : plugin.shopManager.buildStacks(item.itemType, totalItems)) {
                    p.getInventory().addItem(stack);
                }
                p.sendMessage("\u00A7aComprado! \u00A7f" + totalItems + "x " + NpcShopGUI.formatName(item.itemType.name()) + " \u00A7apor \u00A76" + totalPrice + " coins\u00A7a.");
            }
        } else { // SELL
            double totalPrice = item.sellPrice * quantity;

            int count = 0;
            for (org.bukkit.inventory.ItemStack is : p.getInventory().getContents()) {
                if (is != null && is.getType() == item.itemType) count += is.getAmount();
            }
            if (count < totalItems) {
                p.sendMessage("\u00A7cVoce precisa de \u00A7f" + totalItems + "x \u00A7cmas tem apenas \u00A7f" + count + "x\u00A7c.");
                return;
            }
            int toRemove = totalItems;
            org.bukkit.inventory.ItemStack[] contents = p.getInventory().getContents();
            for (int i = 0; i < contents.length && toRemove > 0; i++) {
                org.bukkit.inventory.ItemStack is = contents[i];
                if (is == null || is.getType() != item.itemType) continue;
                if (is.getAmount() <= toRemove) {
                    toRemove -= is.getAmount();
                    p.getInventory().setItem(i, null);
                } else {
                    is.setAmount(is.getAmount() - toRemove);
                    toRemove = 0;
                }
            }
            HaskShop.economy.depositPlayer(p.getName(), totalPrice);
            p.sendMessage("\u00A7eVendido! \u00A7f" + totalItems + "x " + NpcShopGUI.formatName(item.itemType.name()) + " \u00A7epor \u00A76" + totalPrice + " coins\u00A7e.");
        }
    }
}
