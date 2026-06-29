package com.hask.shop.gui;

import com.gmail.nossr50.datatypes.skills.SkillType;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SkillSelectGUI {

    public static void open(Player player, Runnable onSelect) {
        List<ItemStack> skillItems = new ArrayList<>();

        for (SkillType skill : SkillType.values()) {
            if (skill.isChildSkill()) continue;
            skillItems.add(buildSkillItem(skill));
        }

        PaginatedGUI gui = new PaginatedGUI(
            "Selecione a Skill",
            "Clique na skill para ativar o booster 2x",
            skillItems,
            (p, slotIndex, realIndex, item) -> {
                String skillName = ChatColor.stripColor(item.getItemMeta().getDisplayName()).toUpperCase();
                SkillType selected = SkillType.getSkill(skillName);
                if (selected != null) {
                    p.closeInventory();
                    onSelect.run();
                    com.hask.shop.HaskShop.instance.boosterManager.activate(p, selected);
                }
            }
        );

        gui.open(player);
    }

    private static ItemStack buildSkillItem(SkillType skill) {
        Material icon = getSkillIcon(skill);
        String color = getSkillColor(skill);

        List<String> lore = new ArrayList<>();
        lore.add("\u00A77Clique para ativar o booster");
        lore.add("\u00A772x XP por 30 minutos");
        lore.add("");
        lore.add("\u00A7eNivel maximo: \u00A7f" + skill.getMaxLevel());

        ItemStack is = PaginatedGUI.make(icon, (short) 0, color + skill.getName(), lore);
        return is;
    }

    private static Material getSkillIcon(SkillType skill) {
        switch (skill) {
            case MINING:      return Material.DIAMOND_PICKAXE;
            case WOODCUTTING: return Material.IRON_AXE;
            case HERBALISM:   return Material.LEAVES;
            case EXCAVATION:  return Material.DIAMOND_SPADE;
            case FISHING:     return Material.FISHING_ROD;
            case ARCHERY:     return Material.BOW;
            case SWORDS:      return Material.DIAMOND_SWORD;
            case AXES:        return Material.DIAMOND_AXE;
            case TAMING:      return Material.BONE;
            case REPAIR:      return Material.ANVIL;
            case ACROBATICS:  return Material.LEATHER_BOOTS;
            case ALCHEMY:     return Material.BREWING_STAND_ITEM;
            case SMELTING:    return Material.FURNACE;
            case SALVAGE:     return Material.IRON_BLOCK;
            case UNARMED:     return Material.ARROW;
            default:          return Material.NETHER_STAR;
        }
    }

    private static String getSkillColor(SkillType skill) {
        switch (skill) {
            case MINING:
            case WOODCUTTING:
            case HERBALISM:
            case EXCAVATION:
            case FISHING:      return "\u00A7a";
            case ARCHERY:
            case SWORDS:
            case AXES:
            case TAMING:
            case UNARMED:      return "\u00A7c";
            default:           return "\u00A7e";
        }
    }

}
