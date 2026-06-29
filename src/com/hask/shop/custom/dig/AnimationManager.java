package com.hask.shop.custom.dig;

import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockBreakAnimation;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class AnimationManager {

    public static void sendCrack(int animId, int x, int y, int z, int stage) {
        BlockPosition bp = new BlockPosition(x, y, z);
        PacketPlayOutBlockBreakAnimation packet = new PacketPlayOutBlockBreakAnimation(animId, bp, Math.max(stage, -1));
        for (Player p : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public static void clear(int animId, int x, int y, int z) {
        sendCrack(animId, x, y, z, -1);
    }

}
