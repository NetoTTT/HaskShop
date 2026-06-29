package com.hask.shop.custom.item;

import com.hask.shop.custom.CustomItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TotemImortalidadeItem implements Listener {

    private static final String ITEM_ID = "totem_imortalidade";
    private final Set<UUID> ativados = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        UUID uuid = player.getUniqueId();

        if (ativados.contains(uuid)) return;

        double finalHealth = player.getHealth() - event.getFinalDamage();
        if (finalHealth > 0) return;

        PlayerInventory inv = player.getInventory();
        int slot = findTotem(inv);
        if (slot == -1) return;

        event.setCancelled(true);
        player.setHealth(2.0);
        player.setFireTicks(0);
        player.setFoodLevel(20);

        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 900, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 1200, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 600, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 600, 0));

        ItemStack item = inv.getItem(slot);
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            inv.clear(slot);
        }
        player.updateInventory();

        player.getWorld().playEffect(player.getLocation(), org.bukkit.Effect.STEP_SOUND, Material.STONE);
        player.sendMessage("\u00A75\u00A7lTotem \u00A77da Imortalidade ativado!");
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            inv.clear(slot);
        }
        player.updateInventory();

        ativados.add(uuid);
        org.bukkit.Bukkit.getScheduler().runTaskLater(
            com.hask.shop.HaskShop.instance,
            () -> ativados.remove(uuid),
            100L
        );
    }

    private int findTotem(PlayerInventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null || item.getType() == Material.AIR) continue;
            String id = CustomItemBuilder.getId(item);
            String name = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : "sem nome";
            System.out.println("[TotemDebug] Slot " + i + ": type=" + item.getType() + " name=" + name + " hask_id=" + id);
            if (ITEM_ID.equals(id)) return i;
        }
        return -1;
    }

}
