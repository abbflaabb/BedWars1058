

package com.andrei1058.bedwars.arena.upgrades;

import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.events.player.PlayerBaseEnterEvent;
import com.andrei1058.bedwars.api.events.player.PlayerBaseLeaveEvent;
import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.arena.team.BedWarsTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;

import java.util.Map;
import java.util.WeakHashMap;

public class BaseListener implements Listener {

    public static Map<Player, ITeam> isOnABase = new WeakHashMap<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent e) {
        IArena a = Arena.getArenaByIdentifier(e.getPlayer().getWorld().getName());
        if (a == null) return;
        if (a.getStatus() != GameState.playing) return;
        Player p = e.getPlayer();
        checkEvents(p, a);
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent e) {
        Player p = e.getPlayer();
        if (isOnABase.containsKey(p)) {
            IArena a = Arena.getArenaByPlayer(p);
            if (a == null) {
                isOnABase.remove(p);
                return;
            }
            checkEvents(p, a);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        IArena a = Arena.getArenaByPlayer(e.getEntity());
        if (a == null) return;
        checkEvents(e.getEntity(), a);
    }

    /**
     * Check the Enter/ Leave events and call them
     */
    private static void checkEvents(Player player, IArena arena) {
        if (player == null || arena == null || arena.isSpectator(player) || arena.isReSpawning(player)) {
            return;
        }

        boolean notOnBase = true;

        for (ITeam team : arena.getTeams()) {
            if (player.getLocation().distance(team.getBed()) <= arena.getIslandRadius()) {
                notOnBase = false;

                if (isOnABase.containsKey(player)) {
                    ITeam previousTeam = isOnABase.get(player);

                    if (previousTeam != team) {
                        // Player is switching bases, trigger leave event.
                        Bukkit.getPluginManager().callEvent(new PlayerBaseLeaveEvent(player, previousTeam));

                        if (!Arena.magicMilk.containsKey(player.getUniqueId())) {
                            // Player doesn't have magic milk, trigger enter event.
                            Bukkit.getPluginManager().callEvent(new PlayerBaseEnterEvent(player, team));
                        }

                        // Update the player's current base.
                        isOnABase.replace(player, team);
                    }
                } else {
                    // Player was not on any island, trigger enter event
                    if (!Arena.magicMilk.containsKey(player.getUniqueId())) {
                        Bukkit.getPluginManager().callEvent(new PlayerBaseEnterEvent(player, team));
                        isOnABase.put(player, team);
                    }
                }
            }
        }

        // Player has left all bases, trigger leave event if needed.
        if (notOnBase) {
            if (isOnABase.containsKey(player)) {
                ITeam previousTeam = isOnABase.get(player);
                Bukkit.getPluginManager().callEvent(new PlayerBaseLeaveEvent(player, previousTeam));
                isOnABase.remove(player);
            }
        }
    }


    @EventHandler
    public void onBaseEnter(PlayerBaseEnterEvent e) {
        if (e == null) return;
        ITeam team = e.getTeam();
        if (team.isMember(e.getPlayer())) {
            // Give base effects
            for (PotionEffect ef : team.getBaseEffects()) {
                e.getPlayer().addPotionEffect(ef, true);
            }
        } else {
            // Trigger trap
            if (!team.getActiveTraps().isEmpty()) {
                if (!team.isBedDestroyed()) {
                    team.getActiveTraps().get(0).trigger(team, e.getPlayer());
                    team.getActiveTraps().remove(0);
                }
            }
        }
    }

    @EventHandler
    public void onBaseLeave(PlayerBaseLeaveEvent e) {
        if (e == null) return;
        BedWarsTeam t = (BedWarsTeam) e.getTeam();
        if (t.isMember(e.getPlayer())) {
            // Remove effects for members
            for (PotionEffect pef : e.getPlayer().getActivePotionEffects()) {
                for (PotionEffect pf : t.getBaseEffects()) {
                    if (pef.getType() == pf.getType()) {
                        e.getPlayer().removePotionEffect(pf.getType());
                    }
                }
            }
        }
    }

    @EventHandler
    public void onArenaLeave(PlayerLeaveArenaEvent event){
        isOnABase.remove(event.getPlayer());
    }
}