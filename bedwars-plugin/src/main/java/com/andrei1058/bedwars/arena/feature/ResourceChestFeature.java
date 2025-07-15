package com.andrei1058.bedwars.arena.feature;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.andrei1058.bedwars.api.events.player.PlayerItemDepositEvent;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class ResourceChestFeature implements Listener {

    private static ResourceChestFeature instance;
    private final Set<Material> blocked;
    private final Map<IArena, List<Hologram>> holoMap = new HashMap<>();

    private ResourceChestFeature() {
        this.blocked = BedWars.config.getYml()
                .getStringList(ConfigPath.GENERAL_CONFIGURATION_RESOURCE_CHEST_BLOCKED)
                .stream()
                .map(String::toUpperCase)
                .map(Material::valueOf)
                .collect(Collectors.toSet());
        Bukkit.getPluginManager().registerEvents(this, BedWars.plugin);
    }

    public static void init() {
        if (BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_RESOURCE_CHEST_ENABLED)
                && instance == null) {
            instance = new ResourceChestFeature();
        }
    }

    @EventHandler
    public void onArenaStateChange(GameStateChangeEvent e) {
        IArena arena = e.getArena();

        if (e.getNewState() == GameState.restarting) {
            if (holoMap.containsKey(arena)) {
                holoMap.get(arena).forEach(Hologram::remove);
            }
            holoMap.remove(arena);
        } else if (e.getNewState() == GameState.playing) {
            holoMap.put(arena, new ArrayList<>());

            // Check if holograms are enabled in config
            if (BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_RESOURCE_CHEST_HOLOGRAM_ENABLED)) {
                // Force load all chunks in the arena first
                loadArenaChunks(arena);

                // Small delay to ensure chunks are fully loaded
                Bukkit.getScheduler().runTaskLater(BedWars.plugin, () -> createHologramsForArena(arena), 20L); // 1 second delay
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onLeftClickChest(PlayerInteractEvent e) {
        IArena arena = Arena.getArenaByPlayer(e.getPlayer());
        if (arena == null || e.getAction() != Action.LEFT_CLICK_BLOCK) return;

        Block block = e.getClickedBlock();
        if (block == null) return;

        boolean isChest = block.getType() == Material.CHEST;
        boolean isEnderChest = block.getType() == Material.ENDER_CHEST;
        if (!isChest && !isEnderChest) return;

        ITeam team = arena.getTeam(e.getPlayer());
        if (team == null) return;

        Player player = e.getPlayer();
        ItemStack hand = e.getItem();
        if (hand == null || hand.getType() == Material.AIR) return;

        if (blocked.contains(hand.getType())
                || BedWars.nms.isTool(hand)
                || BedWars.nms.getCustomData(hand).equalsIgnoreCase("DEFAULT_ITEM")) {
            String raw = Language.getMsg(player, Messages.RESOURCE_CHEST_BLOCKED_ITEM)
                    .replace("{item}", hand.getType().name().toLowerCase());
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', raw));
            return;
        }

        Inventory inventory = isChest
                ? ((Chest) block.getState()).getBlockInventory()
                : player.getEnderChest();

        if (inventory.firstEmpty() == -1) {
            String raw = Language.getMsg(player, Messages.RESOURCE_CHEST_FULL);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', raw));
            return;
        }

        int inserted = safeDeposit(player, hand, inventory);
        if (inserted <= 0) return;

        if (hand.getType().name().contains("SWORD")) {
            team.defaultSword(player, true);
        }

        String chestType = isEnderChest ? "ender chest" : "team chest";
        String raw = Language.getMsg(player, Messages.RESOURCE_CHEST_DEPOSITED)
                .replace("{amount}", String.valueOf(inserted))
                .replace("{item}", prettyName(hand.getType()))
                .replace("{chest}", chestType);

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', raw));
        player.playSound(player.getLocation(), Sound.CHEST_OPEN, 1.0f, 1.0f);
        callEvent(player, arena, hand.clone(), inventory, isEnderChest);
    }

    private int safeDeposit(Player player, ItemStack hand, Inventory inventory) {
        ItemStack toStore = hand.clone();
        Map<Integer, ItemStack> leftovers = inventory.addItem(toStore);

        int attempted = toStore.getAmount();
        int notInserted = leftovers.values().stream().mapToInt(ItemStack::getAmount).sum();
        int inserted = attempted - notInserted;

        if (inserted <= 0) {
            String raw = Language.getMsg(player, Messages.RESOURCE_CHEST_FULL);
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', raw));
            return 0;
        }

        ItemStack toRemove = hand.clone();
        toRemove.setAmount(inserted);
        player.getInventory().removeItem(toRemove);

        leftovers.values().forEach(player.getInventory()::addItem);
        return inserted;
    }

    private String prettyName(Material mat) {
        String raw = mat.name().replace('_', ' ').toLowerCase();
        return Arrays.stream(raw.split(" "))
                .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1))
                .collect(Collectors.joining(" "));
    }

    private void callEvent(Player player, IArena arena, ItemStack item, Inventory inventory, boolean isEnderChest) {
        Bukkit.getPluginManager().callEvent(new PlayerItemDepositEvent(player, arena, item, inventory, isEnderChest));
    }

    private void loadArenaChunks(IArena arena) {
        // Force load chunks for each team's island
        for (ITeam team : arena.getTeams()) {
            if (team.getSpawn() != null) {
                team.getSpawn().getChunk().load(true);
            }
            if (team.getShop() != null) {
                team.getShop().getChunk().load(true);
            }
            if (team.getTeamUpgrades() != null) {
                team.getTeamUpgrades().getChunk().load(true);
            }
        }

        // Also load spawn area chunks
        if (arena.getSpectatorLocation() != null) {
            arena.getSpectatorLocation().getChunk().load(true);
        }
    }

    private void createHologramsForArena(IArena arena) {
        // Scan all loaded chunks again after forcing chunk loading
        for (Chunk chunk : arena.getWorld().getLoadedChunks()) {
            for (BlockState state : chunk.getTileEntities()) {
                if (state.getType() == Material.CHEST || state.getType() == Material.ENDER_CHEST) {
                    Hologram holo = new Hologram(state.getLocation());
                    holo.createResourceChestHologram(state.getLocation());
                    holoMap.get(arena).add(holo);
                }
            }
        }
    }
}