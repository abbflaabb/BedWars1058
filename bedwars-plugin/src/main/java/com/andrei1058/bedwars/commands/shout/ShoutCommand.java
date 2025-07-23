

package com.andrei1058.bedwars.commands.shout;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class ShoutCommand extends BukkitCommand {

    private static HashMap<UUID, Long> shoutCooldown = new HashMap<>();

    public ShoutCommand(String name) {
        super(name);
    }

    @Override
    public boolean execute(CommandSender s, String st, String[] args) {
        if (s instanceof ConsoleCommandSender) return true;
        Player p = (Player) s;
        IArena a = Arena.getArenaByPlayer(p);
        if (a == null || a.isSpectator(p)) {
            p.sendMessage(Language.getMsg(p, Messages.COMMAND_NOT_FOUND_OR_INSUFF_PERMS));
            return true;
        }
        StringBuilder sb = new StringBuilder();
        for (String ar : args) {
            sb.append(ar).append(" ");
        }

        p.chat("!" + sb.toString());
        return false;
    }

    public static void updateShout(Player player) {
        if (player.hasPermission("bw.shout.bypass")) return;
        if (shoutCooldown.containsKey(player.getUniqueId()))
            shoutCooldown.replace(player.getUniqueId(), System.currentTimeMillis() + (BedWars.config.getYml().getInt(ConfigPath.GENERAL_CONFIGURATION_SHOUT_COOLDOWN, 300) * 1000L));
        else
            shoutCooldown.put(player.getUniqueId(), System.currentTimeMillis() + (BedWars.config.getYml().getInt(ConfigPath.GENERAL_CONFIGURATION_SHOUT_COOLDOWN, 300) * 1000L));
    }

    public static boolean isShoutCooldown(Player player) {
        if (player.hasPermission("bw.shout.bypass")) return false;
        if (!shoutCooldown.containsKey(player.getUniqueId())) return false;
        return shoutCooldown.get(player.getUniqueId()) > System.currentTimeMillis();
    }

    public static double getShoutCooldown(Player p) {
        return (shoutCooldown.get(p.getUniqueId()) - System.currentTimeMillis()) / 1000f;
    }

    public static boolean isShout(Player p) {
        if (!shoutCooldown.containsKey(p.getUniqueId())) return false;
        return shoutCooldown.get(p.getUniqueId()) + 1000 > System.currentTimeMillis();
    }
}
