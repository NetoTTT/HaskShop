package com.hask.shop.custom.item;

import com.hask.shop.custom.CustomItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class TotemMorteItem implements Listener {

    private static final String ITEM_ID = "totem_morte";

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PlayerInventory inv = player.getInventory();

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;

            String id = CustomItemBuilder.getId(item);
            if (!ITEM_ID.equals(id)) continue;

            int usos = CustomItemBuilder.getNbtInt(item, "usos");
            if (usos <= 1) {
                inv.setItem(i, null);
            } else {
                inv.setItem(i, CustomItemBuilder.setNbtInt(item, "usos", usos - 1));
            }

            event.setKeepInventory(true);
            event.setKeepLevel(true);
            event.getDrops().clear();
            event.setDroppedExp(0);

            player.sendMessage("\u00A76\u00A7lTotem \u00A77da Morte ativado! Seus itens foram protegidos.");
            return;
        }
    }

}
