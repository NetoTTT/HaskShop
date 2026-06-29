package com.hask.shop.custom.item;

import com.hask.shop.HaskShop;
import com.hask.shop.custom.CustomItemBuilder;
import com.hask.shop.custom.dig.AnimationManager;
import com.hask.shop.custom.dig.DigManager;
import com.hask.shop.custom.dig.DigSession;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

public class SilkTouch2Item implements Listener {

    private static final String ITEM_ID = "picareta_silk2";
    private static final int MAX_PROGRESS = 100;
    private static final double SPEED = 8.0;

    private final DigManager digManager;

    public SilkTouch2Item(DigManager digManager) {
        this.digManager = digManager;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockDamage(BlockDamageEvent event) {
        Block block = event.getBlock();
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (block.getType() != Material.BEDROCK) return;
        if (!HaskShop.instance.bedrockTracker.isPlaced(block)) return;

        ItemStack tool = player.getItemInHand();
        if (tool == null || tool.getType() != Material.DIAMOND_PICKAXE) return;

        String id = CustomItemBuilder.getId(tool);
        if (!ITEM_ID.equals(id)) return;

        event.setCancelled(true);

        int bx = block.getX(), by = block.getY(), bz = block.getZ();
        int animId = (player.getUniqueId().hashCode() + block.hashCode()) & 0x7FFFFFFF;

        digManager.startDig(new DigSession(
            player, bx, by, bz, animId, MAX_PROGRESS, SPEED,
            () -> {
                HaskShop.instance.bedrockTracker.remove(block);
                block.setType(Material.AIR);
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.BEDROCK));
                block.getWorld().playEffect(block.getLocation(), org.bukkit.Effect.STEP_SOUND, Material.BEDROCK);

                ItemStack held = player.getItemInHand();
                if (held != null && held.getType() == Material.DIAMOND_PICKAXE) {
                    int unbreaking = held.getEnchantmentLevel(Enchantment.DURABILITY);
                    if (unbreaking <= 0 || ThreadLocalRandom.current().nextInt(unbreaking + 1) == 0) {
                        held.setDurability((short) (held.getDurability() + 1));
                        if (held.getDurability() >= held.getType().getMaxDurability()) {
                            player.setItemInHand(null);
                        }
                    }
                }
            }
        ));
    }

}
