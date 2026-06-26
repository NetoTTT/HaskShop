package com.hask.shop;

import com.hask.shop.custom.CustomItemBuilder;
import com.hask.shop.custom.CustomItemRegistry;
import com.hask.shop.gui.EditGUI;
import com.hask.shop.NpcShopManager;
import org.bukkit.inventory.ItemStack;
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
            sender.sendMessage("§cSomente jogadores.");
            return true;
        }
        Player p = (Player) sender;
        // /hs shop é publico - qualquer player pode usar (chamado pelo Citizens)
        if (args.length > 0 && args[0].equalsIgnoreCase("shop")) {
            // tratado no switch abaixo sem checar permissao
        } else if (!p.hasPermission("shopsign.admin")) {
            p.sendMessage("§cVocê não tem permissão.");
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
                if (args.length < 2) { p.sendMessage("§cUso: §f/hs shop <ID>"); return true; }
                String shopId = args[1];

                NpcShopManager.NpcShop npcShop = plugin.npcShopManager.getShop(shopId);
                if (npcShop == null) { p.sendMessage("§cLoja §f" + shopId + " §cnao encontrada."); return true; }
                if (npcShop.items.isEmpty()) { p.sendMessage("§cEsta loja nao tem itens."); return true; }

                com.hask.shop.gui.NpcShopGUI.open(p, npcShop, 0);
                return true;
            }

            case "info":
                if (plugin.pendingInfo.contains(p.getUniqueId())) {
                    plugin.pendingInfo.remove(p.getUniqueId());
                    p.sendMessage("§7Modo info cancelado.");
                } else {
                    plugin.pendingInfo.add(p.getUniqueId());
                    p.sendMessage("§eModo info ativado! Clique com o botao direito em uma placa.");
                    p.sendMessage("§7Use §f/hs info §7novamente para cancelar.");
                }
                break;

            case "add":
                if (plugin.pendingAdd.contains(p.getUniqueId())) {
                    plugin.pendingAdd.remove(p.getUniqueId());
                    p.sendMessage("§7Modo de criação cancelado.");
                } else {
                    plugin.pendingAdd.add(p.getUniqueId());
                    p.sendMessage("§aModo de criação ativado!");
                    p.sendMessage("§7Clique com o §fbotão direito §7em uma placa para registrá-la como loja.");
                    p.sendMessage("§7Use §f/hs add §7novamente para cancelar.");
                }
                break;

            case "edit":
                if (args.length < 2) { p.sendMessage("§cUso: §f/hs edit <ID>"); return true; }
                try {
                    int id = Integer.parseInt(args[1]);
                    ShopData shop = plugin.shopManager.getById(id);
                    if (shop == null) { p.sendMessage("§cLoja §f#" + id + " §cnão encontrada."); return true; }
                    EditGUI.open(p, shop);
                } catch (NumberFormatException e) {
                    p.sendMessage("§cID inválido.");
                }
                break;

            case "remove":
                if (args.length < 2) { p.sendMessage("§cUso: §f/hs remove <ID>"); return true; }
                try {
                    int id = Integer.parseInt(args[1]);
                    if (plugin.shopManager.remove(id)) {
                        p.sendMessage("§cLoja §f#" + id + " §cremovida!");
                    } else {
                        p.sendMessage("§cLoja §f#" + id + " §cnão encontrada.");
                    }
                } catch (NumberFormatException e) {
                    p.sendMessage("§cID inválido.");
                }
                break;

            case "give": {
                // /hs give <player> <itemId> [amount]
                if (args.length < 3) { p.sendMessage("§cUso: §f/hs give <player> <itemId> [amount]"); return true; }
                org.bukkit.entity.Player target = plugin.getServer().getPlayerExact(args[1]);
                if (target == null) { p.sendMessage("§cJogador §f" + args[1] + " §cnao encontrado."); return true; }
                String itemId = args[2];
                ItemStack custom = plugin.customItemRegistry.get(itemId);
                if (custom == null) { p.sendMessage("§cItem §f" + itemId + " §cnao encontrado em custom-items.yml."); return true; }
                int amount = args.length >= 4 ? parseInt(args[3], 1) : custom.getAmount();
                ItemStack toGive = custom.clone();
                toGive.setAmount(Math.max(1, amount));
                target.getInventory().addItem(toGive);
                p.sendMessage("§aDado §f" + amount + "x §a[" + itemId + "] §apara §f" + target.getName() + "§a.");
                if (!p.equals(target)) target.sendMessage("§aVoce recebeu §f" + amount + "x §a[" + itemId + "]§a.");
                break;
            }

            case "items": {
                // /hs items - lista itens customizados
                if (plugin.customItemRegistry.getAll().isEmpty()) {
                    p.sendMessage("§7Nenhum item customizado em custom-items.yml.");
                    return true;
                }
                p.sendMessage("§6§l=== Itens Customizados ===");
                for (java.util.Map.Entry<String, ItemStack> entry : plugin.customItemRegistry.getAll().entrySet()) {
                    String displayName = entry.getValue().getItemMeta().hasDisplayName()
                        ? entry.getValue().getItemMeta().getDisplayName() : "§f" + entry.getKey();
                    p.sendMessage("§f" + entry.getKey() + " §8| " + displayName + " §8| §7" + entry.getValue().getType().name());
                }
                break;
            }

            case "nbt": {
                // /hs nbt - mostra NBT do item na mao
                ItemStack held = p.getItemInHand();
                if (held == null || held.getType() == org.bukkit.Material.AIR) {
                    p.sendMessage("§cSegure um item na mao.");
                    return true;
                }
                try {
                    net.minecraft.server.v1_8_R3.ItemStack nms = org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack.asNMSCopy(held);
                    net.minecraft.server.v1_8_R3.NBTTagCompound tag = nms.getTag();
                    if (tag == null || tag.isEmpty()) {
                        p.sendMessage("§7Este item nao tem NBT.");
                        return true;
                    }
                    p.sendMessage("§6§l=== NBT: " + held.getType().name() + " ===");
                    for (String key : tag.c()) {
                        net.minecraft.server.v1_8_R3.NBTBase val = tag.get(key);
                        p.sendMessage("§f" + key + " §8= §a" + val);
                    }
                } catch (Exception ex) {
                    p.sendMessage("§cErro ao ler NBT: " + ex.getMessage());
                }
                return true;
            }

            case "reload":
                plugin.reload();
                p.sendMessage("§aHaskShop recarregado! §f" + plugin.npcShopManager.getAll().size() + " NPC(s), §f" + plugin.customItemRegistry.getAll().size() + " item(s) customizado(s).");
                break;

            case "npcshop":
                if (plugin.npcShopManager.getAll().isEmpty()) {
                    p.sendMessage("§7Nenhuma loja de NPC configurada em npc-shops.yml.");
                    return true;
                }
                p.sendMessage("§6§l=== Lojas de NPC ===");
                for (NpcShopManager.NpcShop shop : plugin.npcShopManager.getAll().values()) {
                    p.sendMessage("§f" + shop.shopId + " §8| " + shop.name + " §8| §7" + shop.items.size() + " spawner(s)");
                }
                break;

            case "list":
                if (plugin.shopManager.count() == 0) {
                    p.sendMessage("§7Nenhuma loja registrada.");
                    return true;
                }
                p.sendMessage("§6§l=== Lojas Registradas ===");
                for (ShopData d : plugin.shopManager.getAll()) {
                    String status = d.enabled ? "§aATIVA" : "§cDESATIV.";
                    p.sendMessage("§f#" + d.id + " " + status + " §8| §f" + d.type + " §7" + d.amount + "x " + d.item.name() + " §8por §6" + d.price + " §8| §7" + d.world + " " + d.x + "," + d.y + "," + d.z);
                }
                break;

            default:
                sendHelp(p);
        }
        return true;
    }

    private int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }

    private void sendHelp(Player p) {
        p.sendMessage("§6§l=== HaskShop ===");
        p.sendMessage("§f/hs add §7- Ativa modo de criacao (clique numa placa)");
        p.sendMessage("§f/hs info §7- Clique numa placa para ver o ID e config");
        p.sendMessage("§f/hs edit <ID> §7- Edita a loja via GUI");
        p.sendMessage("§f/hs remove <ID> §7- Remove uma loja");
        p.sendMessage("§f/hs list §7- Lista todas as lojas");
        p.sendMessage("§f/hs npcshop §7- Lista lojas de NPC configuradas");
        p.sendMessage("§f/hs items §7- Lista itens customizados (NBT)");
        p.sendMessage("§f/hs give <player> <id> [qty] §7- Da um item customizado");
        p.sendMessage("§f/hs nbt §7- Mostra NBT do item na mao");
        p.sendMessage("§f/hs reload §7- Recarrega todas as configs");
    }
}
