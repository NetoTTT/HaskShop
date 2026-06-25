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
            event.getClicker().sendMessage("§cEsta NPC nao tem loja configurada.");
            event.getClicker().sendMessage("§7Adicione o ID §f" + npcId + " §7ao §fnpc-shops.yml §7e recarregue com §f/cs reload§7.");
            return;
        }

        if (shop.items.isEmpty()) {
            event.getClicker().sendMessage("§cEsta loja nao tem itens configurados.");
            return;
        }

        NpcShopGUI.open(event.getClicker(), shop, 0);
    }
}
