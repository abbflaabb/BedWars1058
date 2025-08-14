package com.andrei1058.bedwars.arena.feature;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;


public class AntiDropFeature implements Listener {
    private static AntiDropFeature instance;
    public AntiDropFeature() {
        Bukkit.getPluginManager().registerEvents(this, BedWars.plugin);
    }

    public static void init() {
        if (BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_ENABLE_ANTI_DROP)) {
            if (instance == null) {
                instance = new AntiDropFeature();
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        if (!BedWars.getAPI().getArenaUtil().isPlaying(player)) {
            return;
        }
        if (!Arena.getArenaByPlayer(player).getStatus().equals(GameState.playing)) {
            return;
        }
        if (player.getVelocity().getY() < -0.5) {
            e.setCancelled(true);
        }
    }
}