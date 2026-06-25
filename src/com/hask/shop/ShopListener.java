package com.hask.shop;

import com.hask.shop.gui.ConfirmGUI;
import com.hask.shop.gui.EditGUI;
import com.hask.shop.SpawnerUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.UUID;

public class ShopListener implements Listener {
    private final HaskShop plugin;

    public ShopListener(HaskShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block b = e.getClickedBlock();
        if (b == null || !(b.getState() instanceof Sign)) return;

        Player p = e.getPlayer();

        // Modo info: /cs info
        if (plugin.pendingInfo.contains(p.getUniqueId())) {
            plugin.pendingInfo.remove(p.getUniqueId());
            e.setCancelled(true);
            ShopData info = plugin.shopManager.getByLocation(b.getLocation());
            if (info == null) {
                p.sendMessage("§cEsta placa nao e uma loja registrada.");
            } else {
                p.sendMessage("§6§l=== Loja #" + info.id + " ===");
                p.sendMessage("§fTipo: §e" + info.type);
                p.sendMessage("§fItem: §e" + info.item.name() + " x" + info.amount);
                p.sendMessage("§fPreco: §e" + info.price + " coins");
                p.sendMessage("§fQtd livre: §e" + (info.askQuantity ? "SIM" : "NAO"));
                p.sendMessage("§fStatus: " + (info.enabled ? "§aATIVA" : "§cDESATIVADA"));
                p.sendMessage("§fLocal: §7" + info.world + " " + info.x + "," + info.y + "," + info.z);
                p.sendMessage("§7Use §f/cs edit " + info.id + " §7para editar.");
            }
            return;
        }

        // Modo de criacao: /cs add
        if (plugin.pendingAdd.contains(p.getUniqueId())) {
            plugin.pendingAdd.remove(p.getUniqueId());
            e.setCancelled(true);
            int id = plugin.shopManager.register(b.getLocation());
            p.sendMessage("§a§lLoja criada! §fID: §e#" + id);
            p.sendMessage("§7Use §f/cs edit " + id + " §7para configurar.");
            return;
        }

        // Click em loja registrada
        ShopData shop = plugin.shopManager.getByLocation(b.getLocation());
        if (shop == null) return;
        e.setCancelled(true);

        if (!shop.enabled) {
            p.sendMessage("§cEsta loja esta desativada.");
            return;
        }

        // Cooldown de 2 segundos
        long now = System.currentTimeMillis();
        Long last = plugin.cooldowns.get(p.getUniqueId());
        if (last != null && now - last < 2000) return;
        plugin.cooldowns.put(p.getUniqueId(), now);

        if (shop.askQuantity) {
            plugin.pendingPurchaseQty.put(p.getUniqueId(), shop);
            String verb = shop.type.equals("BUY") ? "comprar" : "vender";
            p.sendMessage("§eQuantos voce quer " + verb + "? §7(unidade = §f" + shop.amount + "x§7)");
            p.sendMessage("§7Digite §ccancel §7para cancelar.");
        } else {
            executeTransaction(p, shop, 1);
        }
    }

    private void executeTransaction(Player p, ShopData shop, int qty) {
        if (shop.type.equals("BUY")) {
            plugin.shopManager.executeBuy(p, shop, qty);
        } else {
            plugin.shopManager.executeSell(p, shop, qty);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();

        // Entrada de quantidade para compra/venda (placa)
        ShopData qtyShop = plugin.pendingPurchaseQty.get(p.getUniqueId());
        if (qtyShop != null) {
            e.setCancelled(true);
            String input = e.getMessage().trim();
            if (input.equalsIgnoreCase("cancel")) {
                plugin.pendingPurchaseQty.remove(p.getUniqueId());
                p.sendMessage("§7Cancelado.");
                return;
            }
            boolean isAll = isAll(input);
            int qty;
            if (isAll) {
                if (qtyShop.type.equals("SELL")) {
                    int count = countItems(p, qtyShop.item);
                    qty = count / qtyShop.amount;
                    if (qty < 1) { p.sendMessage("§cVoce nao tem §f" + qtyShop.amount + "x §c" + qtyShop.item.name() + " §cno inventario."); return; }
                } else {
                    int space = plugin.shopManager.freeSpace(p, qtyShop.item);
                    int maxBySpace = space / qtyShop.amount;
                    int maxByMoney = (int)(HaskShop.economy.getBalance(p.getName()) / qtyShop.price);
                    qty = Math.min(maxBySpace, maxByMoney);
                    if (qty < 1) { p.sendMessage("§cCoins ou espaco insuficiente para comprar."); return; }
                }
            } else {
                try { qty = Integer.parseInt(input); } catch (NumberFormatException ex) { p.sendMessage("§cDigite um numero ou §fall§c/§ftodos§c/§ftudo§c/§fmax§c para o maximo."); return; }
                if (qty < 1) { p.sendMessage("§cA quantidade deve ser pelo menos §f1§c."); return; }
            }
            plugin.pendingPurchaseQty.remove(p.getUniqueId());
            final ShopData shop = qtyShop;
            final int finalQty = qty;
            if (shop.type.equals("BUY")) {
                int space = plugin.shopManager.freeSpace(p, shop.item);
                if (space < shop.amount * qty) {
                    p.sendMessage("§cInventario cheio! Espaco para §f" + space + " §citens, precisa de §f" + (shop.amount * qty) + "§c.");
                    return;
                }
            }
            plugin.pendingConfirm.put(p.getUniqueId(), new ConfirmSession(qtyShop.id, qty));
            plugin.getServer().getScheduler().runTask(plugin, () -> ConfirmGUI.open(p, shop, finalQty));
            return;
        }

        // Entrada de quantidade para loja de NPC (quantity_free)
        com.hask.shop.NpcQtySession npcQtySession = plugin.pendingNpcQty.get(p.getUniqueId());
        if (npcQtySession != null) {
            e.setCancelled(true);
            String input = e.getMessage().trim();
            if (input.equalsIgnoreCase("cancel")) {
                plugin.pendingNpcQty.remove(p.getUniqueId());
                p.sendMessage("§7Cancelado.");
                return;
            }
            boolean isAll = isAll(input);
            int qty;
            if (isAll) {
                if (npcQtySession.transactionType.equals("SELL")) {
                    qty = countItems(p, npcQtySession.item.itemType);
                    if (qty < 1) { p.sendMessage("§cVoce nao tem §f" + npcQtySession.item.itemType.name() + " §cno inventario."); return; }
                } else {
                    int space = plugin.shopManager.freeSpace(p, npcQtySession.item.itemType);
                    int maxByMoney = (int)(HaskShop.economy.getBalance(p.getName()) / npcQtySession.item.buyPrice);
                    qty = Math.min(space, maxByMoney);
                    if (qty < 1) { p.sendMessage("§cCoins ou espaco insuficiente para comprar."); return; }
                }
            } else {
                try { qty = Integer.parseInt(input); } catch (NumberFormatException ex) { p.sendMessage("§cDigite um numero ou §fall§c/§ftodos§c/§ftudo§c/§fmax§c para o maximo."); return; }
                if (qty < 1) { p.sendMessage("§cA quantidade deve ser pelo menos §f1§c."); return; }
            }
            plugin.pendingNpcQty.remove(p.getUniqueId());
            final com.hask.shop.NpcQtySession s = npcQtySession;
            final int finalQty = qty;
            plugin.pendingNpcConfirm.put(p.getUniqueId(),
                new com.hask.shop.NpcConfirmSession(s.shopId, s.item, s.transactionType, finalQty));
            plugin.getServer().getScheduler().runTask(plugin,
                () -> com.hask.shop.gui.NpcConfirmGUI.open(p, s.item, s.transactionType, finalQty));
            return;
        }

        // Entrada de dados para edicao admin
        EditSession session = plugin.pendingEdit.get(p.getUniqueId());
        if (session == null) return;

        e.setCancelled(true);
        String input = e.getMessage().trim();

        if (input.equalsIgnoreCase("cancel")) {
            plugin.pendingEdit.remove(p.getUniqueId());
            p.sendMessage("§7Edicao cancelada.");
            return;
        }

        plugin.pendingEdit.remove(p.getUniqueId());
        ShopData shop = plugin.shopManager.getById(session.shopId);
        if (shop == null) return;

        switch (session.field) {
            case "item":
                Material mat = Material.getMaterial(input.toUpperCase());
                if (mat == null || mat == Material.AIR) {
                    p.sendMessage("§cItem invalido: §f" + input);
                    p.sendMessage("§7Use o nome em ingles maiusculo. Ex: §fDIAMOND§7, §fIRON_INGOT");
                    return;
                }
                shop.item = mat;
                plugin.shopManager.save();
                p.sendMessage("§aItem definido: §f" + mat.name());
                break;
            case "price":
                try {
                    double price = Double.parseDouble(input.replace(",", "."));
                    if (price < 0) { p.sendMessage("§cO preco nao pode ser negativo."); return; }
                    shop.price = price;
                    plugin.shopManager.save();
                    p.sendMessage("§aPreco definido: §f" + price + " coins");
                } catch (NumberFormatException ex) {
                    p.sendMessage("§cNumero invalido: §f" + input);
                    return;
                }
                break;
            case "amount":
                try {
                    int amount = Integer.parseInt(input);
                    if (amount < 1 || amount > 64) { p.sendMessage("§cQuantidade deve ser entre §f1 §ce §f64§c."); return; }
                    shop.amount = amount;
                    plugin.shopManager.save();
                    p.sendMessage("§aQuantidade definida: §f" + amount);
                } catch (NumberFormatException ex) {
                    p.sendMessage("§cNumero invalido: §f" + input);
                    return;
                }
                break;
        }

        final ShopData finalShop = shop;
        plugin.getServer().getScheduler().runTask(plugin, () -> EditGUI.open(p, finalShop));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        if (!(b.getState() instanceof Sign)) return;
        ShopData shop = plugin.shopManager.getByLocation(b.getLocation());
        if (shop == null) return;

        e.setCancelled(true);
        Player p = e.getPlayer();
        if (p.hasPermission("shopsign.admin")) {
            p.sendMessage("§cEsta placa e uma loja (§f#" + shop.id + "§c). Use §f/cs remove " + shop.id + " §cprimeiro.");
        } else {
            p.sendMessage("§cVoce nao pode quebrar esta placa.");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();
        plugin.pendingAdd.remove(uuid);
        plugin.pendingInfo.remove(uuid);
        plugin.pendingEdit.remove(uuid);
        plugin.pendingPurchaseQty.remove(uuid);
        plugin.pendingConfirm.remove(uuid);
        plugin.pendingNpcQty.remove(uuid);
        plugin.pendingNpcConfirm.remove(uuid);
        plugin.cooldowns.remove(uuid);
    }

    private static boolean isAll(String input) {
        switch (input.toLowerCase()) {
            case "all":
            case "todos":
            case "tudo":
            case "everything":
            case "max":
            case "maximo":
            case "máximo":
            case "total":
            case "full":
                return true;
            default:
                return false;
        }
    }

    private int countItems(Player p, Material mat) {
        int count = 0;
        for (org.bukkit.inventory.ItemStack is : p.getInventory().getContents()) {
            if (is != null && is.getType() == mat) count += is.getAmount();
        }
        return count;
    }
}
