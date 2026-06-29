package com.hask.shop.custom.booster;

import com.gmail.nossr50.events.experience.McMMOPlayerXpGainEvent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DebugXpCommand implements CommandExecutor, Listener {

    private static final DecimalFormat FMT = new DecimalFormat("#,##0.0");
    private final Set<UUID> debugMode = new HashSet<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;
        UUID uuid = p.getUniqueId();

        if (debugMode.contains(uuid)) {
            debugMode.remove(uuid);
            p.sendMessage("\u00A77Debug XP desativado.");
        } else {
            debugMode.add(uuid);
            p.sendMessage("\u00A7aDebug XP ativado! XP aparecera na action bar.");
        }
        return true;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onXpGain(McMMOPlayerXpGainEvent event) {
        Player player = event.getPlayer();
        if (!debugMode.contains(player.getUniqueId())) return;

        String skillName = event.getSkill().getName();
        float xp = event.getRawXpGained();
        int level = event.getSkillLevel();

        String msg = "\u00A7a" + skillName + " \u00A78+ \u00A7f" + FMT.format(xp) + " XP \u00A78[Lv \u00A7f" + level + "\u00A78]";

        IChatBaseComponent comp = IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + msg + "\"}");
        PacketPlayOutChat packet = new PacketPlayOutChat(comp, (byte) 2);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

}
