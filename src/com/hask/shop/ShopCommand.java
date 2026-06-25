package com.hask.shop;

import com.hask.shop.gui.EditGUI;
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
        if (!p.hasPermission("shopsign.admin")) {
            p.sendMessage("§cVocê não tem permissão.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(p);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "info":
                if (plugin.pendingInfo.contains(p.getUniqueId())) {
                    plugin.pendingInfo.remove(p.getUniqueId());
                    p.sendMessage("§7Modo info cancelado.");
                } else {
                    plugin.pendingInfo.add(p.getUniqueId());
                    p.sendMessage("§eModo info ativado! Clique com o botao direito em uma placa.");
                    p.sendMessage("§7Use §f/cs info §7novamente para cancelar.");
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
                    p.sendMessage("§7Use §f/cs add §7novamente para cancelar.");
                }
                break;

            case "edit":
                if (args.length < 2) { p.sendMessage("§cUso: §f/cs edit <ID>"); return true; }
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
                if (args.length < 2) { p.sendMessage("§cUso: §f/cs remove <ID>"); return true; }
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

    private void sendHelp(Player p) {
        p.sendMessage("§6§l=== HaskShop ===");
        p.sendMessage("§f/cs add §7- Ativa modo de criacao (clique numa placa)");
        p.sendMessage("§f/cs info §7- Clique numa placa para ver o ID e config");
        p.sendMessage("§f/cs edit <ID> §7- Edita a loja via GUI");
        p.sendMessage("§f/cs remove <ID> §7- Remove uma loja");
        p.sendMessage("§f/cs list §7- Lista todas as lojas");
    }
}
