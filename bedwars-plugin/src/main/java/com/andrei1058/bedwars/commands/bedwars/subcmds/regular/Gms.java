//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.andrei1058.bedwars.commands.bedwars.subcmds.regular;

import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.command.ParentCommand;
import com.andrei1058.bedwars.api.command.SubCommand;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.commands.bedwars.MainCommand;
import com.andrei1058.bedwars.configuration.Permissions;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Gms extends SubCommand {
    public Gms(ParentCommand parent, String name) {
        super(parent, name);
        this.showInList(true);
        this.setDisplayInfo(MainCommand.createTC("§6 ▪ §7/" + MainCommand.getInstance().getName() + " gms", "/" + this.getParent().getName() + " " + this.getSubCommandName(), "§fSwitch to Survival mode.\n§eClick to activate"));
    }

    public boolean execute(String[] args, CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Language.getMsg((Player)null, Messages.COMMAND_GMS_CONSOLE_DENIED));
            return true;
        } else {
            Player player = (Player)sender;
            if (!player.hasPermission(Permissions.PERMISSION_GMS)) {
                player.sendMessage(Language.getMsg(player, Messages.COMMAND_GMS_NO_PERMISSION));
                return true;
            } else if (Arena.isInArena(player)) {
                player.sendMessage(Language.getMsg(player, Messages.COMMAND_GMS_DISABLED));
                return true;
            } else {
                player.setGameMode(GameMode.SURVIVAL);
                player.sendMessage(Language.getMsg(player, Messages.COMMAND_GMS_ENABLED));
                return true;
            }
        }
    }

    public List<String> getTabComplete() {
        return null;
    }

    public boolean canSee(CommandSender sender, BedWars api) {
        if (!(sender instanceof Player)) {
            return false;
        } else {
            Player player = (Player)sender;
            return !Arena.isInArena(player) && this.hasPermission(sender);
        }
    }
}
