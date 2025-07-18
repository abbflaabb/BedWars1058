package com.andrei1058.bedwars.arena.feature;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.api.tasks.AnnouncementTask;
import com.andrei1058.bedwars.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

import static com.andrei1058.bedwars.BedWars.config;

public class AnnouncementFeature implements Runnable, AnnouncementTask {

    private BukkitTask task;
    private final Arena arena;

    // Maps each player to their personalized announcement message list
    private final LinkedHashMap<Player, List<String>> messages = new LinkedHashMap<>();

    private int index = 0;

    public AnnouncementFeature(Arena arena) {
        this.arena = arena;

        // Initially load messages for all current players and spectators
        List<Player> players = arena.getPlayers();
        if (players != null) {
            for (Player player : players) {
                loadMessagesForPlayer(player, Messages.ARENA_IN_GAME_ANNOUNCEMENT);
            }
        }

        List<Player> spectators = arena.getSpectators();
        if (spectators != null) {
            for (Player player : spectators) {
                loadMessagesForPlayer(player, Messages.ARENA_IN_GAME_ANNOUNCEMENT);
            }
        }

        // Schedule task synchronously (safe for Bukkit API calls)
        // Fixed typo: COOLDOWN instead of COOLDOW
        long cooldownTicks = config.getInt(ConfigPath.GENERAL_CONFIGURATION_IN_GAME_ANNOUNCEMENT_COOLDOW) * 20L;
        this.task = Bukkit.getScheduler().runTaskTimer(BedWars.plugin, this, cooldownTicks, cooldownTicks);
    }

    @Override
    public void loadMessagesForPlayer(Player p, String path) {
        List<String> list = Language.getList(p, path);
        if (this.messages.containsKey(p)) {
            this.messages.get(p).addAll(list);
        } else {
            this.messages.put(p, new ArrayList<>(list));
        }
    }

    @Override
    public void addMessageForPlayer(Player p, String message) {
        if (this.messages.containsKey(p)) {
            this.messages.get(p).add(message);
        } else {
            this.messages.put(p, new ArrayList<>(Collections.singletonList(message)));
        }
    }

    @Override
    public void addMessagesForPlayer(Player p, List<String> messages) {
        if (this.messages.containsKey(p)) {
            this.messages.get(p).addAll(messages);
        } else {
            this.messages.put(p, new ArrayList<>(messages));
        }
    }

    /**
     * Update players and spectators dynamically.
     * Adds messages for new players, removes players who left.
     */
    private void updatePlayers() {
        if (arena == null) {
            return;
        }

        Set<Player> all = new HashSet<>();

        // Safely add players - check for null first
        try {
            List<Player> players = arena.getPlayers();
            if (players != null && !players.isEmpty()) {
                all.addAll(players);
            }
        } catch (Exception e) {
            // Handle any exceptions from arena.getPlayers()
        }

        // Safely add spectators - check for null first
        try {
            List<Player> spectators = arena.getSpectators();
            if (spectators != null && !spectators.isEmpty()) {
                all.addAll(spectators);
            }
        } catch (Exception e) {
            // Handle any exceptions from arena.getSpectators()
        }

        // Add new players/spectators with loaded messages
        for (Player p : all) {
            if (p != null && !messages.containsKey(p)) {
                loadMessagesForPlayer(p, Messages.ARENA_IN_GAME_ANNOUNCEMENT);
            }
        }

        // Remove players who left arena or stopped spectating
        messages.keySet().removeIf(p -> p == null || !all.contains(p));
    }

    /**
     * Replace placeholders in message with actual values.
     * You can expand this with more placeholders as needed.
     */
    private String replacePlaceholders(Player p, String msg) {
        if (arena == null || msg == null) return msg;

        // Replace arena name
        if (arena.getArenaName() != null) {
            msg = msg.replace("{arenaName}", arena.getArenaName());
        }

        // Replace players alive count - safe null check
        List<Player> players = arena.getPlayers();
        int playersCount = players != null ? players.size() : 0;
        msg = msg.replace("{playersAlive}", String.valueOf(playersCount));

        // Replace player name
        if (p != null && p.getName() != null) {
            msg = msg.replace("{playerName}", p.getName());
        }

        // Replace spectators count - safe null check
        List<Player> spectators = arena.getSpectators();
        int spectatorsCount = spectators != null ? spectators.size() : 0;
        msg = msg.replace("{spectators}", String.valueOf(spectatorsCount));

        // Replace total players (alive + spectators)
        msg = msg.replace("{totalPlayers}", String.valueOf(playersCount + spectatorsCount));

        // Replace game state
        if (arena.getStatus() != null) {
            msg = msg.replace("{gameState}", arena.getStatus().toString());
        }

        // Add more placeholder replacements here as needed
        return msg;
    }

    @Override
    public void run() {
        if (arena == null) {
            cancel();
            return;
        }

        // Update players list and message cache
        try {
            updatePlayers();
        } catch (Exception e) {
            // If updatePlayers fails, continue with current cached players
            e.printStackTrace();
        }

        if (arena.getStatus() != GameState.playing) return;

        // Send messages to both players and spectators
        Set<Player> allRecipients = new HashSet<>();

        // Safely add players and spectators
        try {
            List<Player> players = arena.getPlayers();
            if (players != null && !players.isEmpty()) {
                allRecipients.addAll(players);
            }
        } catch (Exception e) {
            // Handle any exceptions from arena.getPlayers()
        }

        try {
            List<Player> spectators = arena.getSpectators();
            if (spectators != null && !spectators.isEmpty()) {
                allRecipients.addAll(spectators);
            }
        } catch (Exception e) {
            // Handle any exceptions from arena.getSpectators()
        }

        for (Player player : allRecipients) {
            if (player == null) continue;

            try {
                List<String> playerMessages = messages.get(player);
                if (playerMessages == null || playerMessages.isEmpty()) {
                    loadMessagesForPlayer(player, Messages.ARENA_IN_GAME_ANNOUNCEMENT);
                    playerMessages = messages.get(player);
                }

                if (playerMessages != null && !playerMessages.isEmpty()) {
                    String msg = replacePlaceholders(player, playerMessages.get(index % playerMessages.size()));
                    if (msg != null) {
                        player.sendMessage(msg);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Reload messages if error occurs (for example after reconnect)
                try {
                    loadMessagesForPlayer(player, Messages.ARENA_IN_GAME_ANNOUNCEMENT);
                } catch (Exception ex) {
                    // If even reloading fails, remove this player from messages
                    messages.remove(player);
                }
            }
        }

        index++;
    }

    @Override
    public IArena getArena() {
        return arena;
    }

    @Override
    public BukkitTask getBukkitTask() {
        return task;
    }

    @Override
    public int getTask() {
        return task.getTaskId();
    }

    @Override
    public void cancel() {
        if (task != null) {
            task.cancel();
        }
    }
}