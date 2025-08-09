package com.andrei1058.bedwars.listeners.chat;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ChatAFK implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Arena.afkCheck.remove(player.getUniqueId());

        if (BedWars.getAPI().getAFKUtil().isPlayerAFK(player)) {
            // Go sync because BungeeCord messaging must be on main thread
            Bukkit.getScheduler().runTask(BedWars.plugin, () -> {
                BedWars.getAPI().getAFKUtil().setPlayerAFK(player, false);

                // Send player to lobby
                sendToServer(player, "lobby");
            });
        }
    }

    private void sendToServer(Player player, String serverName) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            out.writeUTF("Connect");
            out.writeUTF(serverName);
            player.sendPluginMessage(BedWars.plugin, "BungeeCord", b.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
