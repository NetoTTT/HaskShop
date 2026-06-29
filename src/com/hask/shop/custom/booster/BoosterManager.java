package com.hask.shop.custom.booster;

import com.gmail.nossr50.datatypes.skills.SkillType;
import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BoosterManager implements Listener, Runnable {

    private static final int BOOSTER_DURATION = 36000; // 30 min em ticks

    private final Map<UUID, BoosterSession> active = new HashMap<>();

    public BoosterManager() {
        Bukkit.getScheduler().runTaskTimer(
            com.hask.shop.HaskShop.instance, this, 20L, 20L
        );
    }

    public boolean activate(Player player, SkillType skill) {
        return activate(player, skill, 2.0);
    }

    public boolean activate(Player player, SkillType skill, double multiplier) {
        if (active.containsKey(player.getUniqueId())) {
            player.sendMessage("\u00A7cVoce ja tem um booster ativo!");
            return false;
        }
        active.put(player.getUniqueId(), new BoosterSession(skill, multiplier, BOOSTER_DURATION));
        player.sendMessage("\u00A7aBooster \u00A7f" + multiplier + "x \u00A7aativado para \u00A7f" + skill.getName() + " \u00A7apor 30 minutos!");
        return true;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onXpGain(McMMOPlayerXpGainEvent event) {
        if (event.isCancelled()) return;

        BoosterSession session = active.get(event.getPlayer().getUniqueId());
        if (session == null) return;
        if (session.skill != event.getSkill()) return;

        float original = event.getRawXpGained();
        event.setRawXpGained(original * (float) session.multiplier);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        active.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public void run() {
        active.entrySet().removeIf(entry -> {
            entry.getValue().remainingTicks--;
            if (entry.getValue().remainingTicks <= 0) {
                Player p = Bukkit.getPlayer(entry.getKey());
                if (p != null) {
                    p.sendMessage("\u00A77Seu booster de \u00A7f" + entry.getValue().skill.getName() + " \u00A7expirou.");
                }
                return true;
            }
            return false;
        });
    }

    public BoosterSession getActive(Player player) {
        return active.get(player.getUniqueId());
    }

    public void remove(Player player) {
        BoosterSession session = active.remove(player.getUniqueId());
        if (session != null) {
            player.sendMessage("\u00A7cSeu booster de \u00A7f" + session.skill.getName() + " \u00A7cfoi removido.");
        }
    }

}
