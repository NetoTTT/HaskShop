package com.hask.shop.custom.item;

import com.hask.shop.custom.CombatTracker;
import com.hask.shop.custom.CustomItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ReparadorItem implements SpecialItem {

    private static final String ITEM_ID = "reparador";
    private final CombatTracker combatTracker;

    public ReparadorItem(CombatTracker combatTracker) {
        this.combatTracker = combatTracker;
    }

    @Override
    public String getId() {
        return ITEM_ID;
    }

    @Override
    public boolean onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (combatTracker.isInCombat(player)) {
            int sec = combatTracker.getRemainingSeconds(player);
            player.sendMessage("\u00A7cVoce esta em combate! Aguarde \u00A7f" + sec + " \u00A7csegundos.");
            player.updateInventory();
            return true;
        }

        int repaired = 0;
        PlayerInventory inv = player.getInventory();

        for (ItemStack stack : inv.getContents()) {
            if (stack != null && stack.getType() != Material.AIR && stack.getType().getMaxDurability() > 0) {
                if (stack.getDurability() > 0) {
                    stack.setDurability((short) 0);
                    repaired++;
                }
            }
        }

        if (repaired == 0) {
            player.sendMessage("\u00A77Nenhum item para reparar.");
            return true;
        }

        player.sendMessage("\u00A76\u00A7lReparador \u00A77usado! \u00A7f" + repaired + " \u00A7bitem(ns) reparado(s).");

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.setItemInHand(null);
        }
        player.updateInventory();

        return true;
    }

}
