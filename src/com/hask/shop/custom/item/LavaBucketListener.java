package com.hask.shop.custom.item;

import com.hask.shop.custom.CustomItemBuilder;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class LavaBucketListener implements Listener {

    private static final String BUCKET_ID = "balde_lava_bedrock";
    private static final long TRACK_TTL = 60_000L;
    private static final int CHECK_RADIUS = 3;

    private final Set<TrackedLava> trackedLava = new HashSet<>();

    public void cleanup() {
        long now = System.currentTimeMillis();
        trackedLava.removeIf(m -> now - m.timestamp > TRACK_TTL);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        ItemStack item = event.getItemStack();
        if (item == null || item.getType() != Material.LAVA_BUCKET) return;

        String id = CustomItemBuilder.getId(item);
        if (!BUCKET_ID.equals(id)) return;

        Block clicked = event.getBlockClicked();
        if (clicked == null) return;
        Block placed = clicked.getRelative(event.getBlockFace());

        trackedLava.add(new TrackedLava(placed.getWorld(), placed.getX(), placed.getY(), placed.getZ(), System.currentTimeMillis()));

        int usos = CustomItemBuilder.getNbtInt(item, "usos");

        event.setCancelled(true);

        placed.setType(Material.LAVA, true);
        placed.getWorld().playEffect(placed.getLocation(), org.bukkit.Effect.STEP_SOUND, Material.LAVA);

        if (usos > 1) {
            ItemStack updated = CustomItemBuilder.setNbtInt(item, "usos", usos - 1);
            event.getPlayer().setItemInHand(updated);
        } else {
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                event.getPlayer().setItemInHand(null);
            }
            event.getPlayer().updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockForm(BlockFormEvent event) {
        if (event.isCancelled()) return;

        Material result = event.getNewState().getType();
        if (result != Material.STONE && result != Material.COBBLESTONE && result != Material.OBSIDIAN) return;

        Block forming = event.getBlock();
        World world = forming.getWorld();
        int x = forming.getX();
        int y = forming.getY();
        int z = forming.getZ();

        if (isNearTrackedLava(world, x, y, z)) {
            event.setCancelled(true);
            forming.setType(Material.BEDROCK);
            world.playEffect(forming.getLocation(), org.bukkit.Effect.STEP_SOUND, Material.LAVA);
        }
    }

    private boolean isNearTrackedLava(World world, int x, int y, int z) {
        cleanup();

        for (TrackedLava tl : trackedLava) {
            if (!tl.world.equals(world)) continue;
            int dx = Math.abs(tl.x - x);
            int dy = Math.abs(tl.y - y);
            int dz = Math.abs(tl.z - z);
            if (dx <= CHECK_RADIUS && dy <= CHECK_RADIUS && dz <= CHECK_RADIUS) {
                return true;
            }
        }
        return false;
    }

    private static class TrackedLava {
        final World world;
        final int x, y, z;
        final long timestamp;

        TrackedLava(World world, int x, int y, int z, long timestamp) {
            this.world = world;
            this.x = x;
            this.y = y;
            this.z = z;
            this.timestamp = timestamp;
        }
    }

}
