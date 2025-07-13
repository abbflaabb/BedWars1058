//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.andrei1058.bedwars.listeners;

import com.andrei1058.bedwars.api.events.shop.ShopBuyEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.PlayerInventory;

public class ShopListener implements Listener {
    @EventHandler
    public void onShopBuy(ShopBuyEvent event) {
        Player player = event.getBuyer();
        PlayerInventory inventory = player.getInventory();
        if (inventory.firstEmpty() == -1) {
            event.setCancelled(true);
            player.sendMessage(String.valueOf(ChatColor.RED) + "Your inventory is full! Cannot buy item.");
        }

    }
}
