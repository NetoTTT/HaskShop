package com.hask.shop.custom;

import com.hask.shop.HaskShop;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlacedBedrockTracker implements Listener {

    private final HaskShop plugin;
    private final Map<UUID, Set<Long>> placed = new ConcurrentHashMap<>();
    private File file;
    private boolean dirty;
    private final Object lock = new Object();

    public PlacedBedrockTracker(HaskShop plugin) {
        this.plugin = plugin;

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            if (dirty) save();
        }, 6000L, 6000L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        if (event.getBlock().getType() == Material.BEDROCK) {
            mark(event.getBlock());
        }
    }

    // --- Overloads com Block ---

    public void mark(Block block) {
        if (block.getType() != Material.BEDROCK) return;
        mark(block.getWorld(), block.getX(), block.getY(), block.getZ());
    }

    public void remove(Block block) {
        remove(block.getWorld(), block.getX(), block.getY(), block.getZ());
    }

    public boolean isPlaced(Block block) {
        return isPlaced(block.getWorld(), block.getX(), block.getY(), block.getZ());
    }

    // --- Implementacao principal ---

    public void mark(World world, int x, int y, int z) {
        synchronized (lock) {
            placed.computeIfAbsent(world.getUID(), k -> ConcurrentHashMap.newKeySet()).add(encode(x, y, z));
            dirty = true;
        }
    }

    public boolean isPlaced(World world, int x, int y, int z) {
        Set<Long> set = placed.get(world.getUID());
        return set != null && set.contains(encode(x, y, z));
    }

    public void remove(World world, int x, int y, int z) {
        synchronized (lock) {
            Set<Long> set = placed.get(world.getUID());
            if (set != null && set.remove(encode(x, y, z))) {
                dirty = true;
                if (set.isEmpty()) {
                    placed.remove(world.getUID());
                }
            }
        }
    }

    // --- Load / Save ---

    public void load() {
        file = new File(plugin.getDataFolder(), "bedrock-placed.yml");
        if (!file.exists()) return;
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        placed.clear();
        for (String raw : cfg.getStringList("locations")) {
            String[] parts = raw.split(",");
            if (parts.length != 4) continue;
            try {
                UUID world = UUID.fromString(parts[0]);
                int x = Integer.parseInt(parts[1]);
                int y = Integer.parseInt(parts[2]);
                int z = Integer.parseInt(parts[3]);
                placed.computeIfAbsent(world, k -> ConcurrentHashMap.newKeySet()).add(encode(x, y, z));
            } catch (IllegalArgumentException e) {
                // Formato antigo: "worldname,x,y,z" — converter para UUID
                World w = Bukkit.getWorld(parts[0]);
                if (w != null) {
                    try {
                        UUID worldUid = w.getUID();
                        int x = Integer.parseInt(parts[1]);
                        int y = Integer.parseInt(parts[2]);
                        int z = Integer.parseInt(parts[3]);
                        placed.computeIfAbsent(worldUid, k -> ConcurrentHashMap.newKeySet()).add(encode(x, y, z));
                    } catch (Exception ignored) {}
                }
            }
        }
        plugin.getLogger().info("PlacedBedrockTracker: " + count() + " locais carregados.");
    }

    public void save() {
        if (file == null) return;
        List<String> rawList;
        synchronized (lock) {
            rawList = new ArrayList<>(count());
            for (Map.Entry<UUID, Set<Long>> entry : placed.entrySet()) {
                UUID worldUid = entry.getKey();
                for (Long encoded : entry.getValue()) {
                    int x = decodeX(encoded);
                    int y = decodeY(encoded);
                    int z = decodeZ(encoded);
                    rawList.add(worldUid.toString() + "," + x + "," + y + "," + z);
                }
            }
        }
        FileConfiguration cfg = new YamlConfiguration();
        cfg.set("locations", rawList);
        synchronized (lock) {
            try {
                cfg.save(file);
                dirty = false;
            } catch (IOException e) {
                plugin.getLogger().warning("Erro ao salvar bedrock-placed.yml: " + e.getMessage());
            }
        }
    }

    // --- Encode / Decode ---

    private long encode(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38)
             | ((long) (z & 0x3FFFFFF) << 12)
             | (y & 0xFFF);
    }

    private int decodeX(long value) {
        int x = (int) (value >> 38);
        if (x >= 0x2000000) x -= 0x4000000;
        return x;
    }

    private int decodeY(long value) {
        return (int) (value & 0xFFF);
    }

    private int decodeZ(long value) {
        int z = (int) ((value >> 12) & 0x3FFFFFF);
        if (z >= 0x2000000) z -= 0x4000000;
        return z;
    }

    // --- Utilitarios ---

    public int count() {
        return placed.values().stream().mapToInt(Set::size).sum();
    }

}
