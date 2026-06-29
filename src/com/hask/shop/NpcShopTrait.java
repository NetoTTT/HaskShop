package com.hask.shop;

import com.hask.shop.gui.NpcShopGUI;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.event.EventHandler;

public class NpcShopTrait extends Trait {

    public NpcShopTrait() {
        super("haskshop");
    }

    @EventHandler
    public void onRightClick(NPCRightClickEvent event) {
        if (!event.getNPC().equals(getNPC())) return;
        event.setCancelled(true);

        String npcId = String.valueOf(getNPC().getId());
        NpcShopManager.NpcShop shop = HaskShop.instance.npcShopManager.getShop(npcId);

        if (shop == null) {
            event.getClicker().sendMessage("\u00A7cEsta NPC nao tem loja configurada.");
            event.getClicker().sendMessage("\u00A77Adicione o ID \u00A7f" + npcId + " \u00A77ao \u00A7fnpc-shops.yml \u00A77e recarregue com \u00A7f/hs reload\u00A77.");
            return;
        }

        if (shop.items.isEmpty()) {
            event.getClicker().sendMessage("\u00A7cEsta loja nao tem itens configurados.");
            return;
        }

        NpcShopGUI.open(event.getClicker(), shop, 0);
    }
}
