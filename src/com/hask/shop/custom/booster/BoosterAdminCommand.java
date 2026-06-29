package com.hask.shop.custom.booster;

import com.gmail.nossr50.datatypes.skills.SkillType;
import com.hask.shop.HaskShop;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BoosterAdminCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("shopsign.admin")) {
            sender.sendMessage("\u00A7cSem permissao!");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("\u00A76\u00A7l=== Booster Admin ===");
            sender.sendMessage("\u00A7f/booster info [player] \u00A77- Ver booster ativo");
            sender.sendMessage("\u00A7f/booster give <player> <skill> \u00A77- Dar booster manual");
            sender.sendMessage("\u00A7f/booster remove <player> \u00A77- Remover booster");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "info":
            case "check":
            case "status": {
                Player target = args.length >= 2 ? Bukkit.getPlayer(args[1]) : (sender instanceof Player ? (Player) sender : null);
                if (target == null) {
                    sender.sendMessage("\u00A7cJogador \u00A7f" + (args.length >= 2 ? args[1] : "") + " \u00A7cnao encontrado.");
                    return true;
                }
                BoosterSession session = HaskShop.instance.boosterManager.getActive(target);
                if (session == null) {
                    sender.sendMessage("\u00A77" + target.getName() + " \u00A7cnao tem booster ativo.");
                } else {
                    int min = session.remainingTicks / 1200;
                    int sec = (session.remainingTicks % 1200) / 20;
                    sender.sendMessage("\u00A7a" + target.getName() + " \u00A77tem booster \u00A7f" + session.multiplier + "x " + session.skill.getName() + " \u00A77por mais \u00A7f" + min + "m " + sec + "s");
                }
                break;
            }
            case "give":
            case "add": {
                if (args.length < 3) {
                    sender.sendMessage("\u00A7cUso: \u00A7f/booster give <player> <skill> [multiplicador]");
                    sender.sendMessage("\u00A77Ex: \u00A7f/booster give Neto MINING 5x");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("\u00A7cJogador \u00A7f" + args[1] + " \u00A7cnao encontrado.");
                    return true;
                }
                SkillType skill = SkillType.getSkill(args[2].toUpperCase());
                if (skill == null || skill.isChildSkill()) {
                    sender.sendMessage("\u00A7cSkill invalida. Skills: \u00A7fMINING, WOODCUTTING, SWORDS, etc.");
                    return true;
                }
                double mult = 2.0;
                if (args.length >= 4) {
                    try {
                        String raw = args[3].replace("x", "").replace("X", "");
                        mult = Double.parseDouble(raw);
                        if (mult < 2.0) {
                            sender.sendMessage("\u00A7cO multiplicador minimo e \u00A7f2x\u00A7c.");
                            return true;
                        }
                    } catch (NumberFormatException e) {
                        sender.sendMessage("\u00A7cMultiplicador invalido: \u00A7f" + args[3]);
                        sender.sendMessage("\u00A77Use formato: \u00A7f3\u00A77, \u00A7f5x\u00A77, \u00A7f10");
                        return true;
                    }
                }
                HaskShop.instance.boosterManager.activate(target, skill, mult);
                sender.sendMessage("\u00A7aBooster \u00A7f" + mult + "x \u00A7a" + skill.getName() + " \u00A7aativado para \u00A7f" + target.getName());
                break;
            }
            case "remove":
            case "delete":
            case "rm": {
                if (args.length < 2) {
                    sender.sendMessage("\u00A7cUso: \u00A7f/booster remove <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("\u00A7cJogador \u00A7f" + args[1] + " \u00A7cnao encontrado.");
                    return true;
                }
                HaskShop.instance.boosterManager.remove(target);
                sender.sendMessage("\u00A7cBooster removido de \u00A7f" + target.getName());
                break;
            }
            default:
                sender.sendMessage("\u00A7cUso: \u00A7f/booster <info|give|remove>");
        }
        return true;
    }

}
