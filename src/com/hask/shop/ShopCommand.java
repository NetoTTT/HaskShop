package com.hask.shop;

import com.hask.shop.custom.CustomItemBuilder;
import com.hask.shop.custom.CustomItemRegistry;
import com.hask.shop.gui.EditGUI;
import com.hask.shop.NpcShopManager;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {
    private final HaskShop plugin;

    public ShopCommand(HaskShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00A7cSomente jogadores.");
            return true;
        }
        Player p = (Player) sender;
        // /hs shop é publico - qualquer player pode usar (chamado pelo Citizens)
        if (args.length > 0 && args[0].equalsIgnoreCase("shop")) {
            // tratado no switch abaixo sem checar permissao
        } else if (!p.hasPermission("shopsign.admin")) {
            p.sendMessage("\u00A7cVocê não tem permissão.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(p);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "shop": {
                // Comando publico - abre loja de NPC (sem permissao de admin)
                // Usado via /npc command add hs shop <shopId> no Citizens
                if (args.length < 2) { p.sendMessage("\u00A7cUso: \u00A7f/hs shop <ID>"); return true; }
                String shopId = args[1];

                NpcShopManager.NpcShop npcShop = plugin.npcShopManager.getShop(shopId);
                if (npcShop == null) { p.sendMessage("\u00A7cLoja \u00A7f" + shopId + " \u00A7cnao encontrada."); return true; }
                if (npcShop.items.isEmpty()) { p.sendMessage("\u00A7cEsta loja nao tem itens."); return true; }

                com.hask.shop.gui.NpcShopGUI.open(p, npcShop, 0);
                return true;
            }

            case "info":
                if (plugin.pendingInfo.contains(p.getUniqueId())) {
                    plugin.pendingInfo.remove(p.getUniqueId());
                    p.sendMessage("\u00A77Modo info cancelado.");
                } else {
                    plugin.pendingInfo.add(p.getUniqueId());
                    p.sendMessage("\u00A7eModo info ativado! Clique com o botao direito em uma placa.");
                    p.sendMessage("\u00A77Use \u00A7f/hs info \u00A77novamente para cancelar.");
                }
                break;

            case "add":
                if (plugin.pendingAdd.contains(p.getUniqueId())) {
                    plugin.pendingAdd.remove(p.getUniqueId());
                    p.sendMessage("\u00A77Modo de criação cancelado.");
                } else {
                    plugin.pendingAdd.add(p.getUniqueId());
                    p.sendMessage("\u00A7aModo de criação ativado!");
                    p.sendMessage("\u00A77Clique com o \u00A7fbotão direito \u00A77em uma placa para registrá-la como loja.");
                    p.sendMessage("\u00A77Use \u00A7f/hs add \u00A77novamente para cancelar.");
                }
                break;

            case "edit":
                if (args.length < 2) { p.sendMessage("\u00A7cUso: \u00A7f/hs edit <ID>"); return true; }
                try {
                    int id = Integer.parseInt(args[1]);
                    ShopData shop = plugin.shopManager.getById(id);
                    if (shop == null) { p.sendMessage("\u00A7cLoja \u00A7f#" + id + " \u00A7cnão encontrada."); return true; }
                    EditGUI.open(p, shop);
                } catch (NumberFormatException e) {
                    p.sendMessage("\u00A7cID inválido.");
                }
                break;

            case "remove":
                if (args.length < 2) { p.sendMessage("\u00A7cUso: \u00A7f/hs remove <ID>"); return true; }
                try {
                    int id = Integer.parseInt(args[1]);
                    if (plugin.shopManager.remove(id)) {
                        p.sendMessage("\u00A7cLoja \u00A7f#" + id + " \u00A7cremovida!");
                    } else {
                        p.sendMessage("\u00A7cLoja \u00A7f#" + id + " \u00A7cnão encontrada.");
                    }
                } catch (NumberFormatException e) {
                    p.sendMessage("\u00A7cID inválido.");
                }
                break;

            case "give": {
                // /hs give <player> <itemId> [amount]
                if (args.length < 3) { p.sendMessage("\u00A7cUso: \u00A7f/hs give <player> <itemId> [amount]"); return true; }
                org.bukkit.entity.Player target = plugin.getServer().getPlayerExact(args[1]);
                if (target == null) { p.sendMessage("\u00A7cJogador \u00A7f" + args[1] + " \u00A7cnao encontrado."); return true; }
                String itemId = args[2];
                ItemStack custom = plugin.customItemRegistry.get(itemId);
                if (custom == null) { p.sendMessage("\u00A7cItem \u00A7f" + itemId + " \u00A7cnao encontrado em custom-items.yml."); return true; }
                int amount = args.length >= 4 ? parseInt(args[3], 1) : custom.getAmount();
                ItemStack toGive = custom.clone();
                toGive.setAmount(Math.max(1, amount));
                target.getInventory().addItem(toGive);
                p.sendMessage("\u00A7aDado \u00A7f" + amount + "x \u00A7a[" + itemId + "] \u00A7apara \u00A7f" + target.getName() + "\u00A7a.");
                if (!p.equals(target)) target.sendMessage("\u00A7aVoce recebeu \u00A7f" + amount + "x \u00A7a[" + itemId + "]\u00A7a.");
                break;
            }

            case "items": {
                // /hs items - lista no chat | /hs items gui - abre GUI
                if (plugin.customItemRegistry.getAll().isEmpty()) {
                    p.sendMessage("\u00A77Nenhum item customizado em custom-items.yml.");
                    return true;
                }
                if (args.length >= 2 && args[1].equalsIgnoreCase("gui")) {
                    com.hask.shop.gui.CustomItemsGUI.open(p);
                } else {
                    p.sendMessage("\u00A76\u00A7l=== Itens Customizados ===");
                    for (java.util.Map.Entry<String, ItemStack> entry : plugin.customItemRegistry.getAll().entrySet()) {
                        String displayName = entry.getValue().getItemMeta().hasDisplayName()
                            ? entry.getValue().getItemMeta().getDisplayName() : "\u00A7f" + entry.getKey();
                        p.sendMessage("\u00A7f" + entry.getKey() + " \u00A78| " + displayName + " \u00A78| \u00A77" + entry.getValue().getType().name());
                    }
                    p.sendMessage("\u00A77Use \u00A7f/hs items gui \u00A77para abrir a GUI.");
                }
                break;
            }

            case "nbt": {
                // /hs nbt - mostra NBT do item na mao
                ItemStack held = p.getItemInHand();
                if (held == null || held.getType() == org.bukkit.Material.AIR) {
                    p.sendMessage("\u00A7cSegure um item na mao.");
                    return true;
                }
                try {
                    net.minecraft.server.v1_8_R3.ItemStack nms = org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack.asNMSCopy(held);
                    net.minecraft.server.v1_8_R3.NBTTagCompound tag = nms.getTag();
                    if (tag == null || tag.isEmpty()) {
                        p.sendMessage("\u00A77Este item nao tem NBT.");
                        return true;
                    }
                    p.sendMessage("\u00A76\u00A7l=== NBT: " + held.getType().name() + " ===");
                    for (String key : tag.c()) {
                        net.minecraft.server.v1_8_R3.NBTBase val = tag.get(key);
                        p.sendMessage("\u00A7f" + key + " \u00A78= \u00A7a" + val);
                    }
                } catch (Exception ex) {
                    p.sendMessage("\u00A7cErro ao ler NBT: " + ex.getMessage());
                }
                return true;
            }

            case "reload":
                plugin.reload();
                p.sendMessage("\u00A7aHaskShop recarregado! \u00A7f" + plugin.npcShopManager.getAll().size() + " NPC(s), \u00A7f" + plugin.customItemRegistry.getAll().size() + " item(s) customizado(s).");
                break;

            case "npcshop":
                if (plugin.npcShopManager.getAll().isEmpty()) {
                    p.sendMessage("\u00A77Nenhuma loja de NPC configurada em npc-shops.yml.");
                    return true;
                }
                p.sendMessage("\u00A76\u00A7l=== Lojas de NPC ===");
                for (NpcShopManager.NpcShop shop : plugin.npcShopManager.getAll().values()) {
                    p.sendMessage("\u00A7f" + shop.shopId + " \u00A78| " + shop.name + " \u00A78| \u00A77" + shop.items.size() + " spawner(s)");
                }
                break;

            case "list":
                if (plugin.shopManager.count() == 0) {
                    p.sendMessage("\u00A77Nenhuma loja registrada.");
                    return true;
                }
                p.sendMessage("\u00A76\u00A7l=== Lojas Registradas ===");
                for (ShopData d : plugin.shopManager.getAll()) {
                    String status = d.enabled ? "\u00A7aATIVA" : "\u00A7cDESATIV.";
                    String itemInfo = d.customItemId != null ? d.customItemId : d.item.name();
                    p.sendMessage("\u00A7f#" + d.id + " " + status + " \u00A78| \u00A7f" + d.type + " \u00A77" + d.amount + "x " + itemInfo + " \u00A78por \u00A76" + d.price + " \u00A78| \u00A77" + d.world + " " + d.x + "," + d.y + "," + d.z);
                }
                break;

            case "glowtest": {
                ItemStack test = new ItemStack(Material.STONE);
                ItemMeta testMeta = test.getItemMeta();
                testMeta.setDisplayName("\u00A7aGlow Test (STONE)");
                testMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                testMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                test.setItemMeta(testMeta);
                p.getInventory().addItem(test);

                ItemStack test2 = new ItemStack(Material.BEDROCK);
                ItemMeta test2Meta = test2.getItemMeta();
                test2Meta.setDisplayName("\u00A7cGlow Test (BEDROCK)");
                test2Meta.addEnchant(Enchantment.DURABILITY, 1, true);
                test2Meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                test2.setItemMeta(test2Meta);
                p.getInventory().addItem(test2);

                ItemStack test4 = new ItemStack(Material.SKULL_ITEM, 1, (short) 4);
                ItemMeta test4Meta = test4.getItemMeta();
                test4Meta.setDisplayName("\u00A75Glow Test (CREEPER HEAD)");
                test4Meta.addEnchant(Enchantment.DURABILITY, 1, true);
                test4Meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                test4.setItemMeta(test4Meta);
                p.getInventory().addItem(test4);

                p.sendMessage("\u00A7aItems adicionados: STONE, BEDROCK, CREEPER HEAD");
                break;
            }

            default:
                sendHelp(p);
        }
        return true;
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }

    private void sendHelp(Player p) {
        p.sendMessage("\u00A76\u00A7l=== HaskShop ===");
        p.sendMessage("\u00A7f/hs add \u00A77- Ativa modo de criacao (clique numa placa)");
        p.sendMessage("\u00A7f/hs info \u00A77- Clique numa placa para ver o ID e config");
        p.sendMessage("\u00A7f/hs edit <ID> \u00A77- Edita a loja via GUI");
        p.sendMessage("\u00A7f/hs remove <ID> \u00A77- Remove uma loja");
        p.sendMessage("\u00A7f/hs list \u00A77- Lista todas as lojas");
        p.sendMessage("\u00A7f/hs npcshop \u00A77- Lista lojas de NPC configuradas");
        p.sendMessage("\u00A7f/hs items \u00A77- Lista itens customizados (NBT)");
        p.sendMessage("\u00A7f/hs give <player> <id> [qty] \u00A77- Da um item customizado");
        p.sendMessage("\u00A7f/hs nbt \u00A77- Mostra NBT do item na mao");
        p.sendMessage("\u00A7f/hs glowtest \u00A77- Testa brilho sem ItemBuilder");
        p.sendMessage("\u00A7f/hs reload \u00A77- Recarrega todas as configs");
    }
}
