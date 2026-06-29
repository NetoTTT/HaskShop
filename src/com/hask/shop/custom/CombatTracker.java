package com.hask.shop.custom;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatTracker implements Listener {

    private static final long COMBAT_TTL = 15_000L;

    private final Map<UUID, Long> lastDamage = new HashMap<>();

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPvP(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Player)) return;

        lastDamage.put(event.getEntity().getUniqueId(), System.currentTimeMillis());
        lastDamage.put(event.getDamager().getUniqueId(), System.currentTimeMillis());
    }

    public boolean isInCombat(Player player) {
        Long last = lastDamage.get(player.getUniqueId());
        return last != null && (System.currentTimeMillis() - last) < COMBAT_TTL;
    }

    public long getRemainingMillis(Player player) {
        Long last = lastDamage.get(player.getUniqueId());
        if (last == null) return 0;
        long remaining = COMBAT_TTL - (System.currentTimeMillis() - last);
        return Math.max(0, remaining);
    }

    public int getRemainingSeconds(Player player) {
        return (int) Math.ceil(getRemainingMillis(player) / 1000.0);
    }

}
