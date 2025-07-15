

package com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.command.ParentCommand;
import com.andrei1058.bedwars.api.command.SubCommand;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.arena.Misc;
import com.andrei1058.bedwars.arena.SetupSession;
import com.andrei1058.bedwars.commands.bedwars.MainCommand;
import com.andrei1058.bedwars.configuration.Permissions;
import net.md_5.bungee.api.chat.ClickEvent;
import org.apache.commons.io.FileUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class DelArena extends SubCommand {

    private static final HashMap<CommandSender, Long> delArenaConfirm = new HashMap<>();

    public DelArena(ParentCommand parent, String name) {
        super(parent, name);
        setPriority(4);
        showInList(true);
        setPermission(Permissions.PERMISSION_DEL_ARENA);
        setDisplayInfo(Misc.msgHoverClick("§6 ▪ §7/" + MainCommand.getInstance().getName() + " "+getSubCommandName()+" §6<worldName>", "§fDelete a map and its configuration.",
                "/" + MainCommand.getInstance().getName() + " "+getSubCommandName(), ClickEvent.Action.SUGGEST_COMMAND));
    }

    @Override
    public boolean execute(String[] args, CommandSender s) {
        assert s != null;
        if (!MainCommand.isLobbySet()) {
            s.sendMessage("§c▪ §7You have to set the lobby location first!");
            return true;
        }
        if (args.length != 1) {
            s.sendMessage("§c▪ §7Usage: §o/" + MainCommand.getInstance().getName() + " delArena <mapName>");
            return true;
        }
        if (!BedWars.getAPI().getRestoreAdapter().isWorld(args[0])) {
            s.sendMessage("§c▪ §7" + args[0] + " doesn't exist as a world folder!");
            return true;
        }
        if (Arena.getArenaByName(args[0]) != null) {
            s.sendMessage("§c▪ §7Please disable it first!");
            return true;
        }
        File ac = new File(BedWars.plugin.getDataFolder(), "/Arenas/" + args[0]+ ".yml");
        if (!ac.exists()) {
            s.sendMessage("§c▪ §7This arena doesn't exist!");
            return true;
        }
        if (delArenaConfirm.containsKey(s)) {
            if (System.currentTimeMillis() - 2000 <= delArenaConfirm.get(s)) {
                BedWars.getAPI().getRestoreAdapter().deleteWorld(args[0]);
                FileUtils.deleteQuietly(ac);
                s.sendMessage("§c▪ §7" + args[0] + " was deleted!");
                return true;
            }
            s.sendMessage("§6 ▪ §7Type again to confirm.");
            delArenaConfirm.replace(s, System.currentTimeMillis());
        } else {
            s.sendMessage("§6 ▪ §7Type again to confirm.");
            delArenaConfirm.put(s, System.currentTimeMillis());
        }
        return true;
    }

    @Override
    public List<String> getTabComplete() {
        List<String> tab = new ArrayList<>();
        File dir = new File(BedWars.plugin.getDataFolder(), "/Arenas");
        if (dir.exists()) {
            File[] fls = dir.listFiles();
            for (File fl : Objects.requireNonNull(fls)) {
                if (fl.isFile()) {
                    if (fl.getName().contains(".yml")) {
                        tab.add(fl.getName().replace(".yml", ""));
                    }
                }
            }
        }
        return tab;
    }

    @Override
    public boolean canSee(CommandSender s, com.andrei1058.bedwars.api.BedWars api) {
        if (s instanceof Player) {
            Player p = (Player) s;
            if (Arena.isInArena(p)) return false;

            if (SetupSession.isInSetupSession(p.getUniqueId())) return false;
        }

        return hasPermission(s);
    }
}