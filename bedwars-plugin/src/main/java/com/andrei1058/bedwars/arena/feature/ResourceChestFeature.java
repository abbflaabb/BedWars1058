package com.andrei1058.bedwars.arena.feature;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.events.player.PlayerItemDepositEvent;
import com.andrei1058.bedwars.api.language.Language;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ResourceChestFeature implements Listener {

    private static ResourceChestFeature instance;

    private final Set<Material> blocked;

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

    @EventHandler(ignoreCancelled = true)
    public void onLeftClickChest(PlayerInteractEvent e) {
        IArena arena = Arena.getArenaByPlayer(e.getPlayer());
        if (arena == null) return;
        if (e.getAction() != Action.LEFT_CLICK_BLOCK) return;

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
            player.sendMessage(Language.getMsg(player, Messages.RESOURCE_CHEST_BLOCKED_ITEM)
                    .replace("{item}", hand.getType().name().toLowerCase()));
            return;
        }

        Inventory inventory = isChest
                ? ((Chest) block.getState()).getBlockInventory()
                : player.getEnderChest();

        if (inventory.firstEmpty() == -1) {
            player.sendMessage(Language.getMsg(player, Messages.RESOURCE_CHEST_FULL));
            return;
        }

        int inserted = safeDeposit(player, hand, inventory);
        if (inserted <= 0) return;

        if (hand.getType().name().contains("SWORD")) {
            team.defaultSword(player, true);
        }

        String chestType = isEnderChest ? "ender chest" : "chest";
        player.sendMessage(Language.getMsg(player, Messages.RESOURCE_CHEST_DEPOSITED)
                .replace("{item}", hand.getType().name().toLowerCase())
                .replace("{amount}", String.valueOf(inserted))
                .replace("{chest}", chestType));

        player.playSound(player.getLocation(), Sound.CHEST_OPEN, 1.0f, 1.0f);
        callEvent(player, arena, hand.clone(), inventory, isEnderChest);
    }

    private int safeDeposit(Player player, ItemStack hand, Inventory inventory) {
        ItemStack toStore = hand.clone();

        Map<Integer, ItemStack> leftovers = inventory.addItem(toStore);

        int attempted = toStore.getAmount();
        int notInserted = leftovers.values().stream()
                .mapToInt(ItemStack::getAmount)
                .sum();
        int inserted = attempted - notInserted;

        if (inserted <= 0) {
            player.sendMessage(Language.getMsg(player, Messages.RESOURCE_CHEST_FULL));
            return 0;
        }

        ItemStack toRemove = hand.clone();
        toRemove.setAmount(inserted);
        player.getInventory().removeItem(toRemove);

        if (!leftovers.isEmpty()) {
            for (ItemStack leftover : leftovers.values()) {
                player.getInventory().addItem(leftover);
            }
        }

        return inserted;
    }

    private void callEvent(Player player, IArena arena, ItemStack item, Inventory inventory, boolean isEnderChest) {
        PlayerItemDepositEvent event = new PlayerItemDepositEvent(player, arena, item, inventory, isEnderChest);
        Bukkit.getPluginManager().callEvent(event);
    }
}
