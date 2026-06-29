package com.hask.shop;

import com.hask.shop.gui.ConfirmGUI;
import com.hask.shop.gui.EditGUI;
import com.hask.shop.SpawnerUtil;
import com.gmail.nossr50.datatypes.skills.SkillType;
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
import org.bukkit.inventory.ItemStack;
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

        // Modo info: /hs info
        if (plugin.pendingInfo.contains(p.getUniqueId())) {
            plugin.pendingInfo.remove(p.getUniqueId());
            e.setCancelled(true);
            ShopData info = plugin.shopManager.getByLocation(b.getLocation());
            if (info == null) {
                p.sendMessage("\u00A7cEsta placa nao e uma loja registrada.");
            } else {
                p.sendMessage("\u00A76\u00A7l=== Loja #" + info.id + " ===");
                p.sendMessage("\u00A7fTipo: \u00A7e" + info.type);
                String itemName = info.customItemId != null ? info.customItemId : info.item.name() + " x" + info.amount;
                p.sendMessage("\u00A7fItem: \u00A7e" + itemName);
                p.sendMessage("\u00A7fPreco: \u00A7e" + info.price + " coins");
                p.sendMessage("\u00A7fQtd livre: \u00A7e" + (info.askQuantity ? "SIM" : "NAO"));
                p.sendMessage("\u00A7fStatus: " + (info.enabled ? "\u00A7aATIVA" : "\u00A7cDESATIVADA"));
                p.sendMessage("\u00A7fLocal: \u00A77" + info.world + " " + info.x + "," + info.y + "," + info.z);
                p.sendMessage("\u00A77Use \u00A7f/hs edit " + info.id + " \u00A77para editar.");
            }
            return;
        }

        // Modo de criacao: /hs add
        if (plugin.pendingAdd.contains(p.getUniqueId())) {
            plugin.pendingAdd.remove(p.getUniqueId());
            e.setCancelled(true);
            int id = plugin.shopManager.register(b.getLocation());
            p.sendMessage("\u00A7a\u00A7lLoja criada! \u00A7fID: \u00A7e#" + id);
            p.sendMessage("\u00A77Use \u00A7f/hs edit " + id + " \u00A77para configurar.");
            return;
        }

        // Click em loja registrada
        ShopData shop = plugin.shopManager.getByLocation(b.getLocation());
        if (shop == null) return;
        e.setCancelled(true);

        if (!shop.enabled) {
            p.sendMessage("\u00A7cEsta loja esta desativada.");
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
            p.sendMessage("\u00A7eQuantos voce quer " + verb + "? \u00A77(unidade = \u00A7f" + shop.amount + "x\u00A77)");
            p.sendMessage("\u00A77Digite \u00A7ccancel \u00A77para cancelar.");
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

        // Selecao de skill do booster
        ItemStack boosterItem = plugin.pendingBooster.get(p.getUniqueId());
        if (boosterItem != null) {
            e.setCancelled(true);
            String input = e.getMessage().trim();
            if (input.equalsIgnoreCase("cancel")) {
                plugin.pendingBooster.remove(p.getUniqueId());
                p.sendMessage("\u00A77Cancelado.");
                return;
            }
            SkillType skill = SkillType.getSkill(input.toUpperCase());
            if (skill == null || skill.isChildSkill()) {
                p.sendMessage("\u00A7cSkill invalida! Digite o nome exato da skill.");
                p.sendMessage("\u00A77Ex: \u00A7fMINING\u00A77, \u00A7fWOODCUTTING\u00A77, \u00A7fSWORDS");
                return;
            }
            plugin.pendingBooster.remove(p.getUniqueId());

            if (boosterItem.getAmount() > 1) {
                boosterItem.setAmount(boosterItem.getAmount() - 1);
            } else {
                p.setItemInHand(null);
            }
            p.updateInventory();

            plugin.boosterManager.activate(p, skill);
            return;
        }

        // Entrada de quantidade para compra/venda (placa)
        ShopData qtyShop = plugin.pendingPurchaseQty.get(p.getUniqueId());
        if (qtyShop != null) {
            e.setCancelled(true);
            String input = e.getMessage().trim();
            if (input.equalsIgnoreCase("cancel")) {
                plugin.pendingPurchaseQty.remove(p.getUniqueId());
                p.sendMessage("\u00A77Cancelado.");
                return;
            }
            boolean isAll = isAll(input);
            int qty;
            if (isAll) {
                if (qtyShop.type.equals("SELL")) {
                    int count = countItems(p, qtyShop.item);
                    qty = count / qtyShop.amount;
                    if (qty < 1) { p.sendMessage("\u00A7cVoce nao tem \u00A7f" + qtyShop.amount + "x \u00A7c" + qtyShop.item.name() + " \u00A7cno inventario."); return; }
                } else {
                    int space = plugin.shopManager.freeSpace(p, qtyShop.item);
                    int maxBySpace = space / qtyShop.amount;
                    int maxByMoney = (int)(HaskShop.economy.getBalance(p.getName()) / qtyShop.price);
                    qty = Math.min(maxBySpace, maxByMoney);
                    if (qty < 1) { p.sendMessage("\u00A7cCoins ou espaco insuficiente para comprar."); return; }
                }
            } else {
                try { qty = Integer.parseInt(input); } catch (NumberFormatException ex) { p.sendMessage("\u00A7cDigite um numero ou \u00A7fall\u00A7c/\u00A7ftodos\u00A7c/\u00A7ftudo\u00A7c/\u00A7fmax\u00A7c para o maximo."); return; }
                if (qty < 1) { p.sendMessage("\u00A7cA quantidade deve ser pelo menos \u00A7f1\u00A7c."); return; }
            }
            plugin.pendingPurchaseQty.remove(p.getUniqueId());
            final ShopData shop = qtyShop;
            final int finalQty = qty;
            if (shop.type.equals("BUY")) {
                int space = plugin.shopManager.freeSpace(p, shop.item);
                if (space < shop.amount * qty) {
                    p.sendMessage("\u00A7cInventario cheio! Espaco para \u00A7f" + space + " \u00A7citens, precisa de \u00A7f" + (shop.amount * qty) + "\u00A7c.");
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
                p.sendMessage("\u00A77Cancelado.");
                return;
            }
            boolean isAll = isAll(input);
            int qty;
            if (isAll) {
                if (npcQtySession.transactionType.equals("SELL")) {
                    qty = countItems(p, npcQtySession.item.itemType);
                    if (qty < 1) { p.sendMessage("\u00A7cVoce nao tem \u00A7f" + npcQtySession.item.itemType.name() + " \u00A7cno inventario."); return; }
                } else {
                    int space = plugin.shopManager.freeSpace(p, npcQtySession.item.itemType);
                    int maxByMoney = (int)(HaskShop.economy.getBalance(p.getName()) / npcQtySession.item.buyPrice);
                    qty = Math.min(space, maxByMoney);
                    if (qty < 1) { p.sendMessage("\u00A7cCoins ou espaco insuficiente para comprar."); return; }
                }
            } else {
                try { qty = Integer.parseInt(input); } catch (NumberFormatException ex) { p.sendMessage("\u00A7cDigite um numero ou \u00A7fall\u00A7c/\u00A7ftodos\u00A7c/\u00A7ftudo\u00A7c/\u00A7fmax\u00A7c para o maximo."); return; }
                if (qty < 1) { p.sendMessage("\u00A7cA quantidade deve ser pelo menos \u00A7f1\u00A7c."); return; }
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
            p.sendMessage("\u00A77Edicao cancelada.");
            return;
        }

        plugin.pendingEdit.remove(p.getUniqueId());
        ShopData shop = plugin.shopManager.getById(session.shopId);
        if (shop == null) return;

        switch (session.field) {
            case "item":
                Material mat = Material.getMaterial(input.toUpperCase());
                if (mat == null || mat == Material.AIR) {
                    p.sendMessage("\u00A7cItem invalido: \u00A7f" + input);
                    p.sendMessage("\u00A77Use o nome em ingles maiusculo. Ex: \u00A7fDIAMOND\u00A77, \u00A7fIRON_INGOT");
                    return;
                }
                shop.item = mat;
                plugin.shopManager.save();
                p.sendMessage("\u00A7aItem definido: \u00A7f" + mat.name());
                break;
            case "price":
                try {
                    double price = Double.parseDouble(input.replace(",", "."));
                    if (price < 0) { p.sendMessage("\u00A7cO preco nao pode ser negativo."); return; }
                    shop.price = price;
                    plugin.shopManager.save();
                    p.sendMessage("\u00A7aPreco definido: \u00A7f" + price + " coins");
                } catch (NumberFormatException ex) {
                    p.sendMessage("\u00A7cNumero invalido: \u00A7f" + input);
                    return;
                }
                break;
            case "amount":
                try {
                    int amount = Integer.parseInt(input);
                    if (amount < 1 || amount > 64) { p.sendMessage("\u00A7cQuantidade deve ser entre \u00A7f1 \u00A7ce \u00A7f64\u00A7c."); return; }
                    shop.amount = amount;
                    plugin.shopManager.save();
                    p.sendMessage("\u00A7aQuantidade definida: \u00A7f" + amount);
                } catch (NumberFormatException ex) {
                    p.sendMessage("\u00A7cNumero invalido: \u00A7f" + input);
                    return;
                }
                break;
            case "customitem":
                if (input.equalsIgnoreCase("none")) {
                    shop.customItemId = null;
                    plugin.shopManager.save();
                    p.sendMessage("\u00A7aItem custom removido. Usando \u00A7f" + shop.item.name() + " \u00A7apuro.");
                } else if (plugin.customItemRegistry.contains(input)) {
                    shop.customItemId = input;
                    shop.item = plugin.customItemRegistry.get(input).getType();
                    plugin.shopManager.save();
                    p.sendMessage("\u00A7aItem custom definido: \u00A7f" + input);
                } else {
                    p.sendMessage("\u00A7cItem custom \u00A7f" + input + " \u00A7cnao encontrado em custom-items.yml.");
                    p.sendMessage("\u00A77Use \u00A7f/hs items \u00A77para listar os disponiveis.");
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
            p.sendMessage("\u00A7cEsta placa e uma loja (\u00A7f#" + shop.id + "\u00A7c). Use \u00A7f/hs remove " + shop.id + " \u00A7cprimeiro.");
        } else {
            p.sendMessage("\u00A7cVoce nao pode quebrar esta placa.");
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
        plugin.pendingBooster.remove(uuid);
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
