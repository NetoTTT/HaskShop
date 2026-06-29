package com.hask.shop.custom.item;

import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.entity.MPlayerColl;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PowerItem implements SpecialItem {

    private static final String ITEM_ID = "poder_instantaneo";

    @Override
    public String getId() {
        return ITEM_ID;
    }

    @Override
    public boolean onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        MPlayer mplayer = MPlayerColl.get().get(player);
        double atual = mplayer.getPower();
        double max = mplayer.getPowerMax();

        if (atual >= max) {
            player.sendMessage("\u00A7cSeu poder ja esta no maximo!");
            return true;
        }

        mplayer.setPower(max);
        player.sendMessage("\u00A7aPoder restaurado para \u00A7f" + (int) max + "\u00A7a!");

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.setItemInHand(null);
        }
        player.updateInventory();

        return true;
    }

}
