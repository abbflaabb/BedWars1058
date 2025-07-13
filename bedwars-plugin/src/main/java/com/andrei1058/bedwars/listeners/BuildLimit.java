//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

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
        Player p = event.getPlayer();
        IArena arena = Arena.getArenaByPlayer(p);
        if (arena != null) {
            if (arena.getStatus() == GameState.playing && event.getBlockPlaced().getLocation().getBlockY() >= arena.getConfig().getInt("max-build-y")) {
                event.getPlayer().sendMessage(String.valueOf(ChatColor.RED) + "Build height limit reached!");
            }

        }
    }
}
