package com.hask.shop.custom.dig;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class DigSession {

    final Player player;
    final int blockX, blockY, blockZ;
    final int animId;
    int progress;
    final int maxProgress;
    final double speed;
    final Runnable onComplete;

    public DigSession(Player player, int blockX, int blockY, int blockZ, int animId, int maxProgress, double speed, Runnable onComplete) {
        this.player = player;
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
        this.animId = animId;
        this.maxProgress = maxProgress;
        this.speed = speed;
        this.onComplete = onComplete;
        this.progress = 0;
    }

    public boolean isSameBlock(int x, int y, int z) {
        return blockX == x && blockY == y && blockZ == z;
    }

    public Location getBlockLocation() {
        return new Location(player.getWorld(), blockX, blockY, blockZ);
    }

}
