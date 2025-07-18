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
    private final LinkedList<Player> playersWithTntInHand = new LinkedList<>();
    private final Map<Player, BukkitTask> countdownTasks = new HashMap<>();
    private final Map<Player, Integer> countdownValues = new HashMap<>();

    private SpoilPlayerTNTFeature() {
        Bukkit.getPluginManager().registerEvents(new TNTListener(), BedWars.plugin);
        Bukkit.getScheduler().runTaskTimer(BedWars.plugin, new ParticleTask(), 20, 1L);
        // Check every tick (20 times per second) for TNT in hand
        Bukkit.getScheduler().runTaskTimer(BedWars.plugin, new HandCheckTask(), 0, 1L);
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

                    // Enhanced countdown sounds based on remaining time
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

                    // Update countdown
                    countdownValues.put(player, count - 1);
                } else {
                    // Countdown finished - explode TNT
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

        // Check if player is still in arena and holding TNT
        IArena arena = Arena.getArenaByPlayer(player);
        if (arena == null || !arena.isPlayer(player)) {
            playersWithTntInHand.remove(player);
            return;
        }

        // Check if player is still holding TNT
        ItemStack itemInHand = player.getItemInHand();
        if (itemInHand == null || itemInHand.getType() != Material.TNT) {
            playersWithTntInHand.remove(player);
            stopCountdown(player);
            return;
        }

        // Remove all TNT from player's inventory
        player.getInventory().remove(Material.TNT);

        // Enhanced explosion sound sequence
        playExplosionSounds(player);

        // Send explosion message using Messages constant
        String message = Language.getMsg(player, Messages.TNT_EXPLODED_MESSAGE);
        player.sendMessage(message);

        // Remove player from TNT list BEFORE creating explosion to prevent cleanup issues
        playersWithTntInHand.remove(player);

        // Create explosion at player's location (reduced power to prevent killing other players)
        // Use a delayed task to ensure player cleanup is complete
        Bukkit.getScheduler().runTaskLater(BedWars.plugin, () -> {
            if (player.isOnline()) {
                // Create explosion that damages/kills only the TNT carrier
                player.setHealth(0.0); // Direct kill instead of explosion damage

                // Create visual explosion effect without damaging other players
                player.getWorld().createExplosion(player.getLocation(), 0.0f, false);
            }
        }, 1L);
    }

    private void playExplosionSounds(Player player) {
        // Play multiple explosion sounds for enhanced effect
        player.playSound(player.getLocation(), Sound.EXPLODE, 2.0f, 0.8f);
        player.playSound(player.getLocation(), Sound.EXPLODE, 1.5f, 1.0f);
        player.playSound(player.getLocation(), Sound.EXPLODE, 1.0f, 1.2f);

        // Add additional dramatic sounds
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

    private static class HandCheckTask implements Runnable {
        @Override
        public void run() {
            // Check all online players for TNT in hand
            for (Player player : Bukkit.getOnlinePlayers()) {
                IArena arena = Arena.getArenaByPlayer(player);
                if (arena == null || !arena.isPlayer(player) || arena.isSpectator(player)) {
                    // Remove player if they're not in game
                    if (instance.playersWithTntInHand.contains(player)) {
                        instance.playersWithTntInHand.remove(player);
                        instance.stopCountdown(player);
                    }
                    continue;
                }

                ItemStack itemInHand = player.getItemInHand();
                boolean holdingTNT = (itemInHand != null && itemInHand.getType() == Material.TNT);
                boolean inTNTList = instance.playersWithTntInHand.contains(player);

                if (holdingTNT && !inTNTList) {
                    // Player just started holding TNT
                    instance.playersWithTntInHand.add(player);
                    instance.startCountdown(player);
                } else if (!holdingTNT && inTNTList) {
                    // Player stopped holding TNT
                    instance.playersWithTntInHand.remove(player);
                    instance.stopCountdown(player);
                }
            }
        }
    }

    private static class ParticleTask implements Runnable {

        @Override
        public void run() {
            for (Player player : instance.playersWithTntInHand) {
                if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) return;
                BedWars.nms.playRedStoneDot(player);
            }
        }
    }

    private static class TNTListener implements Listener {

        @EventHandler
        public void onDie(PlayerKillEvent event) {
            Player victim = event.getVictim();
            if (instance.playersWithTntInHand.contains(victim)) {
                instance.playersWithTntInHand.remove(victim);
                instance.stopCountdown(victim);
            }
        }

        @EventHandler
        public void onLeave(PlayerLeaveArenaEvent event) {
            Player player = event.getPlayer();
            if (instance.playersWithTntInHand.contains(player)) {
                instance.playersWithTntInHand.remove(player);
                instance.stopCountdown(player);
            }
        }

        // These events are no longer needed since we're checking hand continuously

        @EventHandler(ignoreCancelled = true)
        public void onPickUp(PlayerPickupItemEvent event) {
        }

        @EventHandler(ignoreCancelled = true)
        public void onDrop(PlayerDropItemEvent event) {
        }

        @EventHandler(ignoreCancelled = true)
        public void onPlace(BlockPlaceEvent event) {
        }

        @EventHandler(ignoreCancelled = true)
        public void inventorySwitch(InventoryCloseEvent event) {

        }
    }
}