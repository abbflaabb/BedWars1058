package com.andrei1058.bedwars.commands.bedwars.subcmds.General;

import com.andrei1058.bedwars.commands.bedwars.MainCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import static com.andrei1058.bedwars.BedWars.plugin;
import static com.andrei1058.bedwars.api.language.Language.getMsg;
import com.andrei1058.bedwars.api.language.Messages;
import org.bukkit.entity.Player;

public class HelpCommand extends BukkitCommand {
    public HelpCommand(String name) {
        super(name);
        this.setPermissionMessage("§cYou do not have permission to use this command.");
        this.description = "Shows the help for BedWars commands";
    }

    Player p;

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (commandSender.hasPermission("bedwars.help")) {
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage("§cThis command can only be used by players.");
                return true;
            }
            p = (Player) commandSender; // <- Add this line

            // Display help messages
            p.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + MainCommand.getDot() + ChatColor.GOLD + plugin.getDescription().getName() + " v" + plugin.getDescription().getVersion() + ChatColor.GRAY + '-' + " " + ChatColor.GREEN + "Main Help Commands for BedWars");
            p.sendMessage("§6BedWars Help:");
            p.sendMessage("§7/help - Show this help message");
            p.sendMessage("§7/bw start - Start the game - Rank Use This Command Only S+");
            p.sendMessage("§7/bw join <arena> - Join a specific arena");
            p.sendMessage("§7/bw leave - Leave the current arena");
            p.sendMessage("§7/bw stats - Show your game statistics");

            // Add more commands as needed
            return true;
        }
        if (commandSender instanceof Player) {
            p = (Player) commandSender;
            p.sendMessage(getMsg(p, Messages.Help_Command_No_Perms));
        } else {
            commandSender.sendMessage(getMsg(null, Messages.Help_Command_No_Perms)); // or just a default string
        }
        return false;
    }
}