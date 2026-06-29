package com.hask.shop.custom;

import com.hask.shop.HaskShop;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CustomItemRegistry {

    private final HaskShop plugin;
    private final Map<String, ItemStack> items = new LinkedHashMap<>();
    private File configFile;

    public CustomItemRegistry(HaskShop plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.getDataFolder().mkdirs();
        configFile = new File(plugin.getDataFolder(), "custom-items.yml");

        if (!configFile.exists()) {
            try { configFile.createNewFile(); } catch (IOException e) {
                plugin.getLogger().warning("Nao foi possivel criar custom-items.yml");
            }
        }

        items.clear();
        FileConfiguration cfg;
        try {
            cfg = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(configFile), "UTF-8"));
        } catch (Exception e) {
            plugin.getLogger().warning("Falha ao ler YML como UTF-8, usando encoding padrao: " + e.getMessage());
            cfg = YamlConfiguration.loadConfiguration(configFile);
        }
        ConfigurationSection sec = cfg.getConfigurationSection("items");
        if (sec == null) { plugin.getLogger().info("CustomItemRegistry: nenhum item customizado."); return; }

        for (String id : sec.getKeys(false)) {
            try {
                String matStr = sec.getString(id + ".material", "PAPER").toUpperCase();
                Material mat = Material.valueOf(matStr);
                String name  = sec.getString(id + ".name", "\u00A7f" + id);
                List<String> lore = sec.getStringList(id + ".lore");
                short dataVal = (short) sec.getInt(id + ".data", 0);
                int amount    = sec.getInt(id + ".amount", 1);

                CustomItemBuilder builder = new CustomItemBuilder()
                    .id(id).material(mat).data(dataVal).amount(amount)
                    .name(name).lore(lore);

                // Encantamentos: "SILK_TOUCH:1"
                for (String ench : sec.getStringList(id + ".enchantments")) {
                    String[] parts = ench.split(":");
                    Enchantment e = Enchantment.getByName(parts[0].toUpperCase());
                    int lvl = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
                    if (e != null) builder.enchant(e, lvl);
                }

                // NBT extra
                ConfigurationSection nbtSec = sec.getConfigurationSection(id + ".nbt");
                if (nbtSec != null) {
                    for (String key : nbtSec.getKeys(false)) {
                        Object val = nbtSec.get(key);
                        if (val instanceof Integer) builder.nbt(key, (Integer) val);
                        else builder.nbt(key, String.valueOf(val));
                    }
                }

                items.put(id, builder.build());
            } catch (Exception ex) {
                plugin.getLogger().warning("Erro ao carregar item customizado '" + id + "': " + ex.getMessage());
            }
        }

        plugin.getLogger().info("CustomItemRegistry: " + items.size() + " item(s) customizado(s) carregado(s).");
    }

    public ItemStack get(String id)                      { return items.get(id); }
    public String identify(ItemStack item)               { return CustomItemBuilder.getId(item); }
    public Map<String, ItemStack> getAll()               { return Collections.unmodifiableMap(items); }
    public boolean contains(String id)                   { return items.containsKey(id); }
}
