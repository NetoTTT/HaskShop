package com.hask.shop.custom.item;

import com.hask.shop.gui.SkillSelectGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BoosterItem implements SpecialItem {

    private static final String ITEM_ID = "booster_2x";

    @Override
    public String getId() {
        return ITEM_ID;
    }

    @Override
    public boolean onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        SkillSelectGUI.open(player, () -> {
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.setItemInHand(null);
            }
            player.updateInventory();
        });

        return true;
    }

}
