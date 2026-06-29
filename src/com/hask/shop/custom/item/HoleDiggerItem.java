package com.hask.shop.custom.item;

import com.hask.shop.custom.CustomItemBuilder;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class HoleDiggerItem implements SpecialItem {

    @Override
    public String getId() {
        return "escavador_chunk";
    }

    @Override
    public boolean onInteract(PlayerInteractEvent event) {
        Block clicked = event.getClickedBlock();
        if (clicked == null) return false;

        Player player = event.getPlayer();
        World world = clicked.getWorld();
        ItemStack item = event.getItem();

        int topY = clicked.getY();
        int originX = clicked.getX();
        int originZ = clicked.getZ();

        int chunkX = originX >> 4;
        int chunkZ = originZ >> 4;
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;
        int endX = startX + 16;
        int endZ = startZ + 16;

        int maxY = Math.min(topY + 8, 255);

        int blocksBroken = 0;
        for (int bx = startX; bx < endX; bx++) {
            for (int bz = startZ; bz < endZ; bz++) {
                for (int by = topY; by <= maxY; by++) {
                    Block block = world.getBlockAt(bx, by, bz);
                    if (block.getType() == Material.BEDROCK) continue;
                    if (block.getType() == Material.AIR) continue;

                    block.setType(Material.AIR);
                    blocksBroken++;
                }
            }
        }

        player.sendMessage("\u00A76\u00A7lEscavador \u00A77de chunk! \u00A7f" + blocksBroken + " \u00A77blocos removidos.");

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.setItemInHand(null);
        }
        player.updateInventory();

        return true;
    }

}
