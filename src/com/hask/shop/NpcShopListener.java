package com.hask.shop;

import com.hask.shop.gui.NpcShopGUI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NpcShopListener implements Listener {
    private final HaskShop plugin;

    public NpcShopListener(HaskShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNPCRightClick(NPCRightClickEvent event) {
        String npcId = String.valueOf(event.getNPC().getId());
        Player p = event.getClicker();

        NpcShopManager.NpcShop shop = plugin.npcShopManager.getShop(npcId);
        if (shop == null) return;

        event.setCancelled(true);

        if (shop.items.isEmpty()) {
            p.sendMessage("§cEsta loja nao tem itens configurados.");
            return;
        }

        NpcShopGUI.open(p, shop, 0);
    }
}
