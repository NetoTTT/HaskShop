package com.hask.shop.custom.dig;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DigManager implements Listener, Runnable {

    private final Map<UUID, DigSession> sessions = new HashMap<>();

    public DigManager() {
        Bukkit.getScheduler().runTaskTimer(
            com.hask.shop.HaskShop.instance, this, 1L, 1L
        );
    }

    public void startDig(DigSession session) {
        DigSession old = sessions.remove(session.player.getUniqueId());
        if (old != null) AnimationManager.clear(old.animId, old.blockX, old.blockY, old.blockZ);
        sessions.put(session.player.getUniqueId(), session);
    }

    public void stopDig(Player player) {
        DigSession session = sessions.remove(player.getUniqueId());
        if (session != null) {
            AnimationManager.clear(session.animId, session.blockX, session.blockY, session.blockZ);
        }
    }

    @Override
    public void run() {
        if (sessions.isEmpty()) return;

        for (Map.Entry<UUID, DigSession> entry : sessions.entrySet()) {
            DigSession session = entry.getValue();
            Player player = session.player;

            if (!player.isOnline() || player.isDead()) {
                stopDig(player);
                continue;
            }

            Block target = getTarget(player, 6);
            if (target == null
                || target.getX() != session.blockX
                || target.getY() != session.blockY
                || target.getZ() != session.blockZ
                || target.getType() == Material.AIR) {
                stopDig(player);
                continue;
            }

            session.progress = (int) Math.min(session.progress + session.speed, session.maxProgress);

            int stage = (int) ((double) session.progress / session.maxProgress * 9);
            AnimationManager.sendCrack(session.animId, session.blockX, session.blockY, session.blockZ, stage);

            if (session.progress >= session.maxProgress) {
                entry.getValue().onComplete.run();
                stopDig(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent event) {
        stopDig(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onItemHeld(PlayerItemHeldEvent event) {
        stopDig(event.getPlayer());
    }

    private Block getTarget(Player player, int maxDist) {
        Location eye = player.getEyeLocation();
        Vector dir = eye.getDirection();
        for (double d = 0; d <= maxDist; d += 0.5) {
            double x = eye.getX() + dir.getX() * d;
            double y = eye.getY() + dir.getY() * d;
            double z = eye.getZ() + dir.getZ() * d;
            Block block = player.getWorld().getBlockAt((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
            if (block.getType() != Material.AIR) return block;
        }
        return null;
    }

}
