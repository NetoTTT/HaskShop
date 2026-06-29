package com.hask.shop.custom.item;

import com.hask.shop.custom.CustomItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class LauncherItem implements SpecialItem {

    private static final String ITEM_ID = "lancador";

    @Override
    public String getId() {
        return ITEM_ID;
    }

    @Override
    public boolean onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        player.setVelocity(player.getVelocity().setY(1.5));
        player.sendMessage("\u00A76\u00A7lLa\u00E7ador \u00A77ativado!");

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.setItemInHand(null);
        }
        player.updateInventory();

        return true;
    }

}
