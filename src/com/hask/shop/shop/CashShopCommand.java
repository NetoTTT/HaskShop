package com.hask.shop.shop;

import com.hask.shop.HaskShop;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CashShopCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("\u00A7cApenas jogadores.");
            return true;
        }
        Player p = (Player) sender;
        CashShopGUI.open(p);
        return true;
    }

}
