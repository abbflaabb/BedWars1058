package com.andrei1058.bedwars.commands.leave;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import com.andrei1058.bedwars.BedWars;

public class LeaveCommand extends BukkitCommand {

    public LeaveCommand(String name) {
        super(name);
    }

    @Override
    public boolean execute(CommandSender s, String st, String[] args) {
        if (!(s instanceof Player p)) return true;
        // Check if player is in the BedWars lobby (not in a game)
        if (com.andrei1058.bedwars.arena.Arena.getArenaByPlayer(p) == null) {
            // Player is in the BedWars lobby, send them to the main lobby via BungeeCord
            p.sendMessage("§eSending you to the main lobby...");
            // Send BungeeCord plugin message to send player to main lobby server
            p.sendPluginMessage(BedWars.plugin, "BungeeCord", createBungeeLobbyMessage());
            return true;
        }
        p.sendMessage("§eYou will leave in §c3§e seconds! Don't move.");
        final double startX = p.getLocation().getX();
        final double startY = p.getLocation().getY();
        final double startZ = p.getLocation().getZ();
        new BukkitRunnable() {
            int ticks = 60;
            @Override
            public void run() {
                if (!p.isOnline()) {
                    cancel();
                    return;
                }
                if (p.getLocation().getX() != startX || p.getLocation().getY() != startY || p.getLocation().getZ() != startZ) {
                    p.sendMessage("§cCancelled! You moved.");
                    cancel();
                    return;
                }
                if (--ticks <= 0) {
                    Bukkit.dispatchCommand(p, "bw leave");
                    cancel();
                }
            }
        }.runTaskTimer(BedWars.plugin, 0L, 1L);
        return true;
    }

    private byte[] createBungeeLobbyMessage() {
        java.io.ByteArrayOutputStream b = new java.io.ByteArrayOutputStream();
        java.io.DataOutputStream out = new java.io.DataOutputStream(b);
        try {
            out.writeUTF("Connect");
            out.writeUTF("lobby"); // Change "lobby" to your main lobby server name if different
        } catch (Exception e) {
            e.printStackTrace();
        }
        return b.toByteArray();
    }
}
