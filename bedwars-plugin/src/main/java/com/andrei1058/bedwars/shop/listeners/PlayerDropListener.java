
package com.andrei1058.bedwars.shop.listeners;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.arena.Arena;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerDropListener implements Listener {

    @EventHandler
    //Prevent from dropping permanent items
    public void onDrop(PlayerDropItemEvent e) {
        IArena a = Arena.getArenaByPlayer(e.getPlayer());
        if (a == null) return;
        String identifier = BedWars.nms.getShopUpgradeIdentifier(e.getItemDrop().getItemStack());
        if (identifier == null) return;
        if (identifier.isEmpty() || identifier.equals(" ")) return;
        if (identifier.equals("null")) return;
        e.setCancelled(true);
    }

    @EventHandler
    //Prevent from moving items in chests
    public void onClose(InventoryCloseEvent e) {
        if (!(e instanceof Player)) return;
        IArena a = Arena.getArenaByPlayer((Player) e.getPlayer());
        if (a == null) return;
        String identifier;
        for (ItemStack i : e.getInventory()) {
            if (i == null) continue;
            if (i.getType() == Material.AIR) continue;
            identifier = BedWars.nms.getShopUpgradeIdentifier(i);
            if (identifier.isEmpty() || identifier.equals(" ")) return;
        }
    }
}
