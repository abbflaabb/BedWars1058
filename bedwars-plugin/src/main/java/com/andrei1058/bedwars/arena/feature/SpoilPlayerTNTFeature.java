package com.andrei1058.bedwars.arena.feature;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.api.language.Language;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class SpoilPlayerTNTFeature {

    private static SpoilPlayerTNTFeature instance;
    private final LinkedList<Player> playersWithTnt = new LinkedList<>();
    private final Map<Player, BukkitTask> countdownTasks = new HashMap<>();
    private final Map<Player, Integer> countdownValues = new HashMap<>();

    private SpoilPlayerTNTFeature() {
        Bukkit.getPluginManager().registerEvents(new TNTListener(), BedWars.plugin);
        Bukkit.getScheduler().runTaskTimer(BedWars.plugin, new ParticleTask(), 20, 1L);
    }

    public static void init() {
        if (BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_PERFORMANCE_SPOIL_TNT_PLAYERS))
            if (instance == null) instance = new SpoilPlayerTNTFeature();
    }

    private void startCountdown(Player player) {
        // Cancel any existing countdown
        stopCountdown(player);

        countdownValues.put(player, 3);

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || !countdownValues.containsKey(player)) {
                    cancel();
                    return;
                }

                int count = countdownValues.get(player);

                if (count > 0) {
                    // Display countdown using Messages constant
                    String message = Language.getMsg(player, Messages.TNT_COUNTDOWN_MESSAGE).replace("{time}", String.valueOf(count));
                    player.sendMessage(message);

                    if (count == 3) {
                        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.0f, 1.0f);
                    } else if (count == 2) {
                        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.2f, 1.2f);
                        player.playSound(player.getLocation(), Sound.CLICK, 0.8f, 1.5f);
                    } else if (count == 1) {
                        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1.5f, 1.5f);
                        player.playSound(player.getLocation(), Sound.CLICK, 1.0f, 1.8f);
                        player.playSound(player.getLocation(), Sound.ANVIL_USE, 0.5f, 2.0f);

                        // Warning sound for final second
                        player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 0.3f, 2.0f);
                    }

                    countdownValues.put(player, count - 1);
                } else {
                    explodeTNT(player);

                    // Clean up
                    countdownValues.remove(player);
                    countdownTasks.remove(player);
                    cancel();
                }
            }
        }.runTaskTimer(BedWars.plugin, 0L, 20L); // Run every second

        countdownTasks.put(player, task);
    }

    private void stopCountdown(Player player) {
        BukkitTask task = countdownTasks.remove(player);
        if (task != null) {
            task.cancel();
        }
        countdownValues.remove(player);
    }

    private void explodeTNT(Player player) {
        if (!player.isOnline()) return;

        player.getInventory().remove(Material.TNT);

        playExplosionSounds(player);

        // Create explosion at player's location
        player.getWorld().createExplosion(player.getLocation(), 4.0f, false);

        String message = Language.getMsg(player, Messages.TNT_EXPLODED_MESSAGE);
        player.sendMessage(message);

        // Remove player from TNT list
        playersWithTnt.remove(player);
    }

    private void playExplosionSounds(Player player) {
        player.playSound(player.getLocation(), Sound.EXPLODE, 2.0f, 0.8f);
        player.playSound(player.getLocation(), Sound.EXPLODE, 1.5f, 1.0f);
        player.playSound(player.getLocation(), Sound.EXPLODE, 1.0f, 1.2f);

        player.playSound(player.getLocation(), Sound.ANVIL_LAND, 1.5f, 0.5f);
        player.playSound(player.getLocation(), Sound.ZOMBIE_WOOD, 2.0f, 0.3f);

        // Play sounds for nearby players too
        for (Player nearby : player.getWorld().getPlayers()) {
            if (nearby.equals(player)) continue;
            double distance = nearby.getLocation().distance(player.getLocation());
            if (distance <= 20) {
                float volume = (float) Math.max(0.1, 2.0 - (distance / 10.0));
                float pitch = (float) (0.8 + (Math.random() * 0.4)); // Random pitch variation

                nearby.playSound(player.getLocation(), Sound.EXPLODE, volume, pitch);
                nearby.playSound(player.getLocation(), Sound.ANVIL_LAND, volume * 0.7f, 0.5f);
            }
        }

        // Delayed secondary explosion sounds for echo effect
        Bukkit.getScheduler().runTaskLater(BedWars.plugin, () -> {
            if (player.isOnline()) {
                player.playSound(player.getLocation(), Sound.EXPLODE, 0.8f, 0.6f);
                player.playSound(player.getLocation(), Sound.ZOMBIE_WOOD, 1.0f, 0.2f);
            }
        }, 5L);

        Bukkit.getScheduler().runTaskLater(BedWars.plugin, () -> {
            if (player.isOnline()) {
                player.playSound(player.getLocation(), Sound.EXPLODE, 0.4f, 0.4f);
            }
        }, 10L);
    }

    private static class ParticleTask implements Runnable {

        @Override
        public void run() {
            for (Player player : instance.playersWithTnt) {
                if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;
                BedWars.nms.playRedStoneDot(player);
            }
        }
    }

    private static class TNTListener implements Listener {

        @EventHandler
        public void onDie(PlayerKillEvent event) {
            Player victim = event.getVictim();
            instance.playersWithTnt.remove(victim);
            instance.stopCountdown(victim);
        }

        @EventHandler
        public void onLeave(PlayerLeaveArenaEvent event) {
            Player player = event.getPlayer();
            instance.playersWithTnt.remove(player);
            instance.stopCountdown(player);
        }

        @EventHandler(ignoreCancelled = true)
        public void onPickUp(PlayerPickupItemEvent event) {
            if (event.getItem().getItemStack().getType() == Material.TNT) {
                IArena arena = Arena.getArenaByPlayer(event.getPlayer());
                if (arena == null || !arena.isPlayer(event.getPlayer()) || arena.isSpectator(event.getPlayer())) return;
                if (instance.playersWithTnt.contains(event.getPlayer())) return;

                instance.playersWithTnt.add(event.getPlayer());
                instance.startCountdown(event.getPlayer());
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void onDrop(PlayerDropItemEvent event) {
            if (event.getItemDrop().getItemStack().getType() == Material.TNT) {
                IArena arena = Arena.getArenaByPlayer(event.getPlayer());
                if (arena == null || !arena.isPlayer(event.getPlayer()) || arena.isSpectator(event.getPlayer())) return;
                if (!instance.playersWithTnt.contains(event.getPlayer())) return;
                if (event.getPlayer().getInventory().contains(Material.TNT)) return;

                instance.playersWithTnt.remove(event.getPlayer());
                instance.stopCountdown(event.getPlayer());
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void onPlace(BlockPlaceEvent event) {
            ItemStack inHand = event.getItemInHand();
            IArena arena = Arena.getArenaByPlayer(event.getPlayer());
            if (arena == null || !arena.isPlayer(event.getPlayer()) || arena.isSpectator(event.getPlayer())) return;

            if (inHand.getType() == Material.TNT) {
                if (!instance.playersWithTnt.contains(event.getPlayer())) return;

                Bukkit.getScheduler().runTaskLater(BedWars.plugin, () -> {
                    if (!event.getPlayer().getInventory().contains(Material.TNT)) {
                        instance.playersWithTnt.remove(event.getPlayer());
                        instance.stopCountdown(event.getPlayer());
                    }
                }, 1L);
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void inventorySwitch(InventoryCloseEvent event) {
            Player player = (Player) event.getPlayer();
            IArena arena = Arena.getArenaByPlayer(player);
            if (arena == null || !arena.isPlayer(player) || arena.isSpectator(player) || player.isDead()) return;

            if (instance.playersWithTnt.contains(player)) {
                if (player.getInventory().contains(Material.TNT)) return;
                instance.playersWithTnt.remove(player);
                instance.stopCountdown(player);
            } else {
                if (!player.getInventory().contains(Material.TNT)) return;
                instance.playersWithTnt.add(player);
                instance.startCountdown(player);
            }
        }
    }
}