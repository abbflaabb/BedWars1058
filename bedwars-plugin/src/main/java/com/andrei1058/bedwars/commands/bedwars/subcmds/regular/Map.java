//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.andrei1058.bedwars.commands.bedwars.subcmds.regular;

import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.command.ParentCommand;
import com.andrei1058.bedwars.api.command.SubCommand;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.commands.bedwars.MainCommand;
import java.util.List;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Map extends SubCommand {
    public Map(ParentCommand parent, String name) {
        super(parent, name);
        this.showInList(true);
        this.setDisplayInfo(MainCommand.createTC("§6 ▪ §7/" + MainCommand.getInstance().getName() + " map", "/" + this.getParent().getName() + " " + this.getSubCommandName(), "§fDisplay the current map name.\n§eClick to view"));
    }

    public boolean execute(String[] args, CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Language.getMsg((Player)null, Messages.COMMAND_MAP_CONSOLE_DENIED));
            return true;
        } else {
            Player player = (Player)sender;
            IArena arena = Arena.getArenaByPlayer(player);
            if (arena == null) {
                player.sendMessage(Language.getMsg(player, Messages.COMMAND_MAP_NOT_IN_GAME));
            } else {
                String mapName = arena.getDisplayName();
                player.sendMessage(Language.getMsg(player, Messages.COMMAND_MAP_DISPLAY).replace("{map}", mapName));
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0F, 1.0F);
            }

            return true;
        }
    }

    public List<String> getTabComplete() {
        return null;
    }

    public boolean canSee(CommandSender sender, BedWars api) {
        return sender instanceof Player;
    }
}
