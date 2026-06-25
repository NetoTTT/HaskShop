package com.hask.shop;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpawnerUtil {

    public static class MobEntry {
        public final EntityType type;
        public final short eggData;
        public final String ptName;
        public final String color;

        public MobEntry(EntityType type, short eggData, String ptName, String color) {
            this.type = type;
            this.eggData = eggData;
            this.ptName = ptName;
            this.color = color;
        }
    }

    public static final List<MobEntry> MOBS;

    static {
        List<MobEntry> list = new ArrayList<>();
        String h = "§c"; // hostil
        String n = "§e"; // neutro
        String p = "§a"; // passivo

        // Hostis
        list.add(new MobEntry(EntityType.ZOMBIE,      (short)54,  "Zumbi",               h));
        list.add(new MobEntry(EntityType.SKELETON,    (short)51,  "Esqueleto",            h));
        list.add(new MobEntry(EntityType.SPIDER,      (short)52,  "Aranha",               h));
        list.add(new MobEntry(EntityType.CAVE_SPIDER, (short)59,  "Aranha das Cavernas",  h));
        list.add(new MobEntry(EntityType.CREEPER,     (short)50,  "Creeper",              h));
        list.add(new MobEntry(EntityType.BLAZE,       (short)61,  "Blaze",                h));
        list.add(new MobEntry(EntityType.GHAST,       (short)56,  "Ghast",                h));
        list.add(new MobEntry(EntityType.MAGMA_CUBE,  (short)62,  "Cubo de Magma",        h));
        list.add(new MobEntry(EntityType.PIG_ZOMBIE,  (short)57,  "Zumbi Porco",          h));
        list.add(new MobEntry(EntityType.ENDERMAN,    (short)58,  "Enderman",             h));
        list.add(new MobEntry(EntityType.SLIME,       (short)55,  "Slime",                h));
        list.add(new MobEntry(EntityType.WITCH,       (short)66,  "Bruxa",                h));
        list.add(new MobEntry(EntityType.SILVERFISH,  (short)60,  "Traca",                h));
        list.add(new MobEntry(EntityType.ENDERMITE,   (short)67,  "Endermite",            h));
        list.add(new MobEntry(EntityType.GUARDIAN,    (short)68,  "Guardiao",             h));
        // Neutros
        list.add(new MobEntry(EntityType.WOLF,        (short)95,  "Lobo",                 n));
        list.add(new MobEntry(EntityType.BAT,         (short)65,  "Morcego",              n));
        list.add(new MobEntry(EntityType.OCELOT,      (short)98,  "Ocelote",              n));
        // Passivos
        list.add(new MobEntry(EntityType.PIG,         (short)90,  "Porco",                p));
        list.add(new MobEntry(EntityType.SHEEP,       (short)91,  "Ovelha",               p));
        list.add(new MobEntry(EntityType.COW,         (short)92,  "Vaca",                 p));
        list.add(new MobEntry(EntityType.CHICKEN,     (short)93,  "Galinha",              p));
        list.add(new MobEntry(EntityType.SQUID,       (short)94,  "Lula",                 p));
        list.add(new MobEntry(EntityType.MUSHROOM_COW,(short)96,  "Vaca Cogumelo",        p));
        list.add(new MobEntry(EntityType.SNOWMAN,     (short)97,  "Golem de Neve",        p));
        list.add(new MobEntry(EntityType.IRON_GOLEM,  (short)99,  "Golem de Ferro",       p));
        list.add(new MobEntry(EntityType.HORSE,       (short)100, "Cavalo",               p));
        list.add(new MobEntry(EntityType.RABBIT,      (short)101, "Coelho",               p));
        list.add(new MobEntry(EntityType.VILLAGER,    (short)120, "Aldeao",               p));

        MOBS = Collections.unmodifiableList(list);
    }

    public static MobEntry getEntry(String entityTypeName) {
        if (entityTypeName == null) return null;
        for (MobEntry e : MOBS) {
            if (e.type.name().equalsIgnoreCase(entityTypeName)) return e;
        }
        return null;
    }

    public static boolean isValid(String name) {
        return getEntry(name) != null;
    }

    public static ItemStack[] createStacks(String entityTypeName, int total) {
        int numStacks = (int) Math.ceil((double) total / 64);
        ItemStack[] stacks = new ItemStack[numStacks];
        for (int i = 0; i < numStacks; i++) {
            int amt = Math.min(total - i * 64, 64);
            stacks[i] = createOne(entityTypeName, amt);
        }
        return stacks;
    }

    public static ItemStack createOne(String entityTypeName, int amount) {
        ItemStack item = new ItemStack(Material.MOB_SPAWNER, amount);
        try {
            MobEntry entry = getEntry(entityTypeName);
            String displayName = entry != null
                ? "§d§lSpawner §8» " + entry.color + "§l" + entry.ptName
                : "§d§lSpawner de Mob";

            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(displayName);
                item.setItemMeta(meta);
            }

            String nmsName = EntityType.valueOf(entityTypeName.toUpperCase()).getName();
            if (nmsName == null) return item;

            net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
            NBTTagCompound tag = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();
            NBTTagCompound blockTag = new NBTTagCompound();
            blockTag.setString("EntityId", nmsName);
            tag.set("BlockEntityTag", blockTag);
            nmsItem.setTag(tag);
            return CraftItemStack.asBukkitCopy(nmsItem);
        } catch (Exception e) {
            return item;
        }
    }
}
