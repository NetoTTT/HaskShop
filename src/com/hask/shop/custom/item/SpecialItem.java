package com.hask.shop.custom.item;

import org.bukkit.event.player.PlayerInteractEvent;

public interface SpecialItem {

    String getId();

    boolean onInteract(PlayerInteractEvent event);

}
