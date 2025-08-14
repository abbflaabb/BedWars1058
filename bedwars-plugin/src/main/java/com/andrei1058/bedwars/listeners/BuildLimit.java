package com.andrei1058.bedwars.listeners;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.arena.Arena;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BuildLimit implements Listener {
    @EventHandler
    public void playerBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        IArena arena = Arena.getArenaByPlayer(player);
        if (arena == null) return;
        int maxBuildY = arena.getConfig().getInt("max-build-y");
        if (arena.getStatus() == GameState.playing && event.getBlockPlaced().getY() >= maxBuildY) {
            player.sendMessage(ChatColor.RED + "Build height limit reached!");
            event.setCancelled(true);
        }
    }
}
