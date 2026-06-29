package com.hask.shop.custom.item;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;

public class BauExtraItem implements SpecialItem {

    private static final String ITEM_ID = "bau_extra";
    private static final int MAX_SLOTS = 5;

    @Override
    public String getId() {
        return ITEM_ID;
    }

    @Override
    public boolean onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        File dataFile = new File(
            com.hask.shop.HaskShop.instance.getDataFolder().getParentFile(),
            "ExBau/Baus/" + player.getUniqueId().toString() + ".yml"
        );

        if (!dataFile.exists()) {
            player.sendMessage("\u00A7cUse \u00A7f/bau \u00A7cprimeiro para criar seu ba\u00FA.");
            return true;
        }

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(dataFile);

        // Determinar qual secao de slots usar (formato novo ou antigo)
        ConfigurationSection slotsSection = cfg.getConfigurationSection("Config.Baus");
        if (slotsSection == null) {
            slotsSection = cfg.getConfigurationSection("Baus");
        }
        if (slotsSection == null) {
            player.sendMessage("\u00A7cFormato de dados inv\u00E1lido. Contate um admin.");
            return true;
        }

        int slotLiberado = -1;
        for (int i = 1; i <= MAX_SLOTS; i++) {
            if (!slotsSection.contains(String.valueOf(i))
                || !slotsSection.getBoolean(String.valueOf(i), false)) {
                // Slot vazio ou com {} (objeto vazio) = disponivel
                Object val = slotsSection.get(String.valueOf(i));
                if (val == null || val instanceof Boolean && !(Boolean) val || val instanceof ConfigurationSection && ((ConfigurationSection) val).getKeys(false).isEmpty()) {
                    slotLiberado = i;
                    break;
                }
            }
        }

        if (slotLiberado == -1) {
            player.sendMessage("\u00A7cVoce ja liberou todos os ba\u00FAs!");
            return true;
        }

        slotsSection.set(String.valueOf(slotLiberado), true);
        cfg.set("Config.Baus", slotsSection);

        try {
            cfg.save(dataFile);
        } catch (IOException e) {
            player.sendMessage("\u00A7cErro ao salvar. Tente novamente.");
            return true;
        }

        player.sendMessage("\u00A7aBa\u00FA \u00A7f#" + slotLiberado + " \u00A7aLiberado!");

        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.setItemInHand(null);
        }
        player.updateInventory();

        return true;
    }

}
