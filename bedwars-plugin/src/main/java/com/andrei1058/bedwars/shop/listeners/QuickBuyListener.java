
package com.andrei1058.bedwars.shop.listeners;

import com.andrei1058.bedwars.api.events.player.PlayerJoinArenaEvent;
import com.andrei1058.bedwars.api.events.player.PlayerReJoinEvent;
import com.andrei1058.bedwars.shop.quickbuy.PlayerQuickBuyCache;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuickBuyListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onArenaJoin(PlayerJoinArenaEvent e){
        if (e == null) return;
        if (e.isSpectator()) return;
        PlayerQuickBuyCache cache = PlayerQuickBuyCache.getQuickBuyCache(e.getPlayer().getUniqueId());
        if (cache != null) {
            cache.destroy();
        }
        new PlayerQuickBuyCache(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onArenaJoin(PlayerReJoinEvent e){
        if (e == null) return;
        PlayerQuickBuyCache cache = PlayerQuickBuyCache.getQuickBuyCache(e.getPlayer().getUniqueId());
        if (cache != null) {
            cache.destroy();
        }
        new PlayerQuickBuyCache(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onQuit(PlayerQuitEvent e){
        if (e == null) return;
        PlayerQuickBuyCache cache = PlayerQuickBuyCache.getQuickBuyCache(e.getPlayer().getUniqueId());
        if (cache == null) return;
        cache.destroy();
    }
}
