
package com.andrei1058.bedwars.commands.bedwars.subcmds.regular;

import com.andrei1058.bedwars.api.BedWars;
import com.andrei1058.bedwars.api.command.ParentCommand;
import com.andrei1058.bedwars.api.command.SubCommand;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.arena.ArenaGUI;
import com.andrei1058.bedwars.arena.SetupSession;
import com.andrei1058.bedwars.commands.bedwars.MainCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CmdGUI extends SubCommand {

    public CmdGUI(ParentCommand parent, String name) {
        super(parent, name);
        setPriority(17);
        showInList(false);
        setDisplayInfo(com.andrei1058.bedwars.commands.bedwars.MainCommand.createTC("§6 ▪ §7/"+ MainCommand.getInstance().getName()+" "+getSubCommandName(), "/"+getParent().getName()+" "+getSubCommandName(), "§fOpens the arena GUI."));
    }

    @Override
    public boolean execute(String[] args, CommandSender s) {
        if (s instanceof ConsoleCommandSender) return false;
        Player p = (Player) s;
        if (Arena.getArenaByPlayer(p) != null) return false;

        String group = "default";
        if (args.length == 1){
            group = args[0];
        }

        ArenaGUI.openGui(p, group);
        return true;
    }

    @Override
    public List<String> getTabComplete() {
        return null;
    }

    @Override
    public boolean canSee(CommandSender s, BedWars api) {
        if (s instanceof ConsoleCommandSender) return false;

        Player p = (Player) s;
        if (Arena.isInArena(p)) return false;

        if (SetupSession.isInSetupSession(p.getUniqueId())) return false;
        return hasPermission(s);
    }
}
