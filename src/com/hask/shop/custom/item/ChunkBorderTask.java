package com.hask.shop.custom.item;

import com.hask.shop.custom.CustomItemBuilder;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChunkBorderTask implements Runnable, Listener {

    private static final String EXCAVATOR_ID = "escavador_chunk";
    private final Set<UUID> showing = new HashSet<>();

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        ItemStack item = event.getPlayer().getInventory().getItem(event.getNewSlot());
        UUID uuid = event.getPlayer().getUniqueId();
        if (isExcavator(item)) {
            showing.add(uuid);
        } else {
            showing.remove(uuid);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        showing.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (!showing.contains(uuid)) continue;

            ItemStack hand = player.getItemInHand();
            if (!isExcavator(hand)) {
                showing.remove(uuid);
                continue;
            }

            Location loc = player.getLocation();
            int chunkX = loc.getBlockX() >> 4;
            int chunkZ = loc.getBlockZ() >> 4;
            int startX = (chunkX << 4) + 1;
            int startZ = (chunkZ << 4) + 1;
            int endX = startX + 14;
            int endZ = startZ + 14;
            double y = loc.getY();

            for (int x = startX; x <= endX; x++) {
                sendBorderParticle(player, x + 0.5, y + 0.5, startZ - 0.5);
                sendBorderParticle(player, x + 0.5, y + 0.5, endZ + 0.5);
            }
            for (int z = startZ; z <= endZ; z++) {
                sendBorderParticle(player, startX - 0.5, y + 0.5, z + 0.5);
                sendBorderParticle(player, endX + 0.5, y + 0.5, z + 0.5);
            }
        }
    }

    private void sendBorderParticle(Player player, double x, double y, double z) {
        try {
            PacketPlayOutWorldParticles packet = new PacketPlayOutWorldParticles();
            setField(packet, "a", EnumParticle.REDSTONE);
            setField(packet, "b", (float) x);
            setField(packet, "c", (float) y);
            setField(packet, "d", (float) z);
            setField(packet, "e", 1.0F);
            setField(packet, "f", 0.0F);
            setField(packet, "g", 0.0F);
            setField(packet, "h", 1.0F);
            setField(packet, "i", 0);
            setField(packet, "j", false);
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        } catch (Exception ignored) {}
    }

    private void setField(Object obj, String name, Object value) throws Exception {
        Field field = obj.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(obj, value);
    }

    private boolean isExcavator(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        String id = CustomItemBuilder.getId(item);
        return EXCAVATOR_ID.equals(id);
    }

}
