/*
 * ARQUIVADO / DISABLED
 *
 * Item: balde_lava_bedrock
 * Motivo: A mecânica de transformar stone->bedrock na interação lava+água
 *         não funciona de forma confiável no Minecraft 1.8.8 (MrsSpigot).
 *         BlockFormEvent não dispara nessa versão, e a alternativa com
 *         scans periódicos (20+ scans de 7x7x7 a cada 3 ticks) é pesada
 *         demais para rodar em produção.
 *
 * Solução ideal futura: usar NMS para substituir a lógica de formação
 *         de blocos na fonte (net.minecraft.server.v1_8_R3.BlockFluids),
 *         ou esperar upgrade do servidor para versão mais nova.
 *
 * Para reativar:
 *  1. Descomentar em HaskShop.java o registro do lavaBucketItem
 *  2. Descomentar o item 'balde_lava_bedrock' no custom-items.yml
 *  3. Recompilar
 */

package com.hask.shop.custom.item;

import com.hask.shop.custom.CustomItemBuilder;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class LavaBucketItem implements Listener {

    private static final String BUCKET_ID = "balde_lava_bedrock";
    private static final int SCAN_RADIUS = 3;
    private static final int SCAN_INTERVAL = 3;
    private static final int SCAN_REPEATS = 20;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteractDirect(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.LAVA_BUCKET) return;

        String id = CustomItemBuilder.getId(item);
        if (!BUCKET_ID.equals(id)) return;

        event.setCancelled(true);
        handlePlacement(event);
    }

    private void handlePlacement(PlayerInteractEvent event) {
        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;

        int usos = CustomItemBuilder.getNbtInt(item, "usos");
        Block placed = clicked.getRelative(event.getBlockFace());
        World world = placed.getWorld();
        final int px = placed.getX(), py = placed.getY(), pz = placed.getZ();

        placed.setType(Material.LAVA, true);
        world.playEffect(placed.getLocation(), org.bukkit.Effect.STEP_SOUND, Material.LAVA);

        convertAdjacentWater(placed);

        scheduleBedrockScans(world, px, py, pz);

        if (usos > 1) {
            ItemStack updated = CustomItemBuilder.setNbtInt(item, "usos", usos - 1);
            player.setItemInHand(updated);
        } else {
            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                player.setItemInHand(null);
            }
            player.updateInventory();
        }
    }

    private void scheduleBedrockScans(final World world, final int x, final int y, final int z) {
        org.bukkit.Bukkit.getScheduler().runTaskLater(
            com.hask.shop.HaskShop.instance,
            new Runnable() {
                int count = 0;
                public void run() {
                    count++;
                    scanForStone(world, x, y, z);
                    if (count < SCAN_REPEATS) {
                        org.bukkit.Bukkit.getScheduler().runTaskLater(
                            com.hask.shop.HaskShop.instance, this, SCAN_INTERVAL
                        );
                    }
                }
            },
            2L
        );
    }

    private void scanForStone(World world, int x, int y, int z) {
        int converted = 0;
        for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++) {
            for (int dy = -SCAN_RADIUS; dy <= SCAN_RADIUS; dy++) {
                for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
                    Block block = world.getBlockAt(x + dx, y + dy, z + dz);
                    Material type = block.getType();
                    if (type == Material.STONE || type == Material.COBBLESTONE) {
                        block.setType(Material.BEDROCK);
                        converted++;
                    } else if (type == Material.WATER || type == Material.STATIONARY_WATER) {
                        block.setType(Material.BEDROCK);
                        converted++;
                    }
                }
            }
        }
        if (converted > 0) {
            System.out.println("[HaskShop] scan convertidos=" + converted + " em " + x + "," + y + "," + z);
        }
    }

    private void convertAdjacentWater(Block lavaBlock) {
        World world = lavaBlock.getWorld();
        int x = lavaBlock.getX(), y = lavaBlock.getY(), z = lavaBlock.getZ();
        int converted = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) continue;
                    Block adj = world.getBlockAt(x + dx, y + dy, z + dz);
                    Material type = adj.getType();
                    if (type == Material.WATER || type == Material.STATIONARY_WATER) {
                        adj.setType(Material.BEDROCK);
                        converted++;
                    }
                }
            }
        }

        if (converted > 0) {
            System.out.println("[HaskShop] " + converted + " aguas convertidas em bedrock ao redor de " + x + "," + y + "," + z);
        }
    }

}
