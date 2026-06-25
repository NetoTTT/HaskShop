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

        // Entrada de quantidade para compra/venda
        ShopData qtyShop = plugin.pendingPurchaseQty.get(p.getUniqueId());
        if (qtyShop != null) {
            e.setCancelled(true);
            String input = e.getMessage().trim();
            if (input.equalsIgnoreCase("cancel")) {
                plugin.pendingPurchaseQty.remove(p.getUniqueId());
                p.sendMessage("§7Cancelado.");
                return;
            }
            try {
                int qty = Integer.parseInt(input);
                if (qty < 1) {
                    p.sendMessage("§cA quantidade deve ser pelo menos §f1§c.");
                    return;
                }
                plugin.pendingPurchaseQty.remove(p.getUniqueId());
                final ShopData shop = qtyShop;
                final int finalQty = qty;
                // Valida espaco no inventario antes de abrir confirmacao (apenas para compras)
                if (shop.type.equals("BUY")) {
                    int totalItems = shop.amount * qty;
                    int space = plugin.shopManager.freeSpace(p, shop.item);
                    if (space < totalItems) {
                        p.sendMessage("§cInventario cheio! Voce tem espaco para §f" + space + " §citens, mas precisa de §f" + totalItems + "§c.");
                        return;
                    }
                }
                plugin.pendingConfirm.put(p.getUniqueId(), new ConfirmSession(qtyShop.id, qty));
                plugin.getServer().getScheduler().runTask(plugin, () -> ConfirmGUI.open(p, shop, finalQty));
            } catch (NumberFormatException ex) {
                p.sendMessage("§cDigite apenas numeros.");
            }
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
        plugin.cooldowns.remove(uuid);
    }
}
