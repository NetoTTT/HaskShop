package com.hask.shop.custom.item;

import com.hask.shop.custom.CustomItemBuilder;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SpecialItemListener implements Listener {

    private final SpecialItemRegistry registry;

    public SpecialItemListener(SpecialItemRegistry registry) {
        this.registry = registry;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) return;
        if (event.isCancelled()) return;

        ItemStack item = event.getItem();
        if (item == null) return;

        String id = CustomItemBuilder.getId(item);
        if (id == null) return;

        SpecialItem handler = registry.get(id);
        if (handler == null) return;

        event.setCancelled(true);

        handler.onInteract(event);
    }

}
