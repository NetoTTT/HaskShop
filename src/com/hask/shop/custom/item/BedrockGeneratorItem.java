package com.hask.shop.custom.item;

import com.hask.shop.HaskShop;
import com.hask.shop.custom.CustomItemBuilder;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class BedrockGeneratorItem implements SpecialItem {

    private static final int MAX_DEPTH = 20;

    @Override
    public String getId() {
        return "gerador_bedrock";
    }

    @Override
    public boolean onInteract(PlayerInteractEvent event) {
        Block clicked = event.getClickedBlock();
        if (clicked == null) return false;

        Player player = event.getPlayer();
        World world = clicked.getWorld();
        ItemStack item = event.getItem();

        Block placed = clicked.getRelative(event.getBlockFace());
        final int originX = placed.getX();
        final int originY = placed.getY();
        final int originZ = placed.getZ();

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.setItemInHand(null);
        }
        player.updateInventory();

        player.sendMessage("\u00A76\u00A7lGerador \u00A77ativado! Colocando bedrock...");

        final int[] currentY = { originY };
        final int[] taskId = { -1 };
        taskId[0] = org.bukkit.Bukkit.getScheduler().scheduleSyncRepeatingTask(
            com.hask.shop.HaskShop.instance,
            new Runnable() {
                public void run() {
                    int y = currentY[0];
                    if (y < originY - MAX_DEPTH) {
                        org.bukkit.Bukkit.getScheduler().cancelTask(taskId[0]);
                        return;
                    }

                    Block block = world.getBlockAt(originX, y, originZ);
                    if (block.getType() != Material.AIR) {
                        player.sendMessage("\u00A76\u00A7lGerador \u00A77finalizado! Bateu em: \u00A7f" + block.getType());
                        org.bukkit.Bukkit.getScheduler().cancelTask(taskId[0]);
                        return;
                    }

                    block.setType(Material.BEDROCK);
                    HaskShop.instance.bedrockTracker.mark(block);
                    world.playEffect(block.getLocation(), org.bukkit.Effect.STEP_SOUND, Material.BEDROCK);
                    currentY[0] = y - 1;
                }
            },
            0L, 20L
        );

        return true;
    }

}
