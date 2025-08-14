package com.andrei1058.bedwars.support.citizens;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.command.SubCommand;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.arena.Misc;
import com.andrei1058.bedwars.commands.bedwars.MainCommand;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.NPC.createArmorStand;

public class JoinNPC {
    private static boolean citizensSupport = false;

    /* Here are stored NPC holograms without colors and placeholders translated used for refresh*/
    public static final Map<ArmorStand, List<String>> npcsHolograms = new HashMap<>();
    /* Here are stored all the NPCs*/
    public static final Map<Integer, String> npcs = new HashMap<>();


    /**
     * Check if Citizens is loaded correctly
     */
    public static boolean isCitizensSupport() {
        return citizensSupport;
    }

    /**
     * Set Citizens support
     */
    public static void setCitizensSupport(boolean citizensSupport) {
        JoinNPC.citizensSupport = citizensSupport;
        MainCommand bw = MainCommand.getInstance();
        if (bw == null) return;
        if (citizensSupport) {
            //add npc subCommand
            if (bw.isRegistered()) {
                boolean registered = false;
                for (SubCommand sc : bw.getSubCommands()) {
                    if (sc.getSubCommandName().equalsIgnoreCase("npc")) {
                        registered = true;
                        break;
                    }
                }
                if (!registered) {
                    new com.andrei1058.bedwars.commands.bedwars.subcmds.sensitive.NPC(bw, "npc");
                }
            }
        } else {
            //remove npc subCommand
            if (bw.isRegistered()) {
                bw.getSubCommands().removeIf(sc -> sc.getSubCommandName().equalsIgnoreCase("npc"));
            }
        }
    }

    /**
     * Spawn a join-NPC
     *
     * @param group Arena Group
     * @param location     Location where to be spawned
     * @param displayName  Display name
     * @param skin  A player name to get his skin
     */
    @Nullable
    public static NPC spawnNPC(Location location, String displayName, String group, String skin, NPC existingNPC) {
        if (!isCitizensSupport()) return null;
        NPC npc = (existingNPC == null)
                ? CitizensAPI.getNPCRegistry().createNPC(EntityType.PLAYER, "")
                : existingNPC;
        if (!npc.isSpawned()) {
            npc.spawn(location);
        }
        if (npc.getEntity() instanceof SkinnableEntity) {
            ((SkinnableEntity) npc.getEntity()).setSkinName(skin);
        }
        npc.setProtected(true);
        npc.setName("");
        String[] lines = displayName.split("\\\\n");
        // Remove nearby armor stands
        for (Entity entity : location.getWorld().getNearbyEntities(location, 1, 3, 1)) {
            if (entity.getType() == EntityType.ARMOR_STAND) entity.remove();
        }
        npcs.put(npc.getId(), group);
        if (lines.length >= 2) {
            ArmorStand line1 = createArmorStand(location.clone().add(0, 0.05, 0));
            line1.setMarker(false);
            line1.setCustomNameVisible(true);
            line1.setCustomName(ChatColor.translateAlternateColorCodes('&', lines[0]).replace("{players}", String.valueOf(Arena.getPlayers(group))));
            ArmorStand line2 = createArmorStand(location.clone().subtract(0, 0.25, 0));
            line2.setMarker(false);
            line2.setCustomName(ChatColor.translateAlternateColorCodes('&', lines[1].replace("{players}", String.valueOf(Arena.getPlayers(group)))));
            line2.setCustomNameVisible(true);
            npcsHolograms.put(line1, Arrays.asList(group, lines[0]));
            npcsHolograms.put(line2, Arrays.asList(group, lines[1]));
        } else if (lines.length == 1) {
            ArmorStand line = createArmorStand(location.clone().subtract(0, 0.25, 0));
            line.setMarker(false);
            line.setCustomName(ChatColor.translateAlternateColorCodes('&', lines[0]).replace("{players}", String.valueOf(Arena.getPlayers(group))));
            line.setCustomNameVisible(true);
            npcsHolograms.put(line, Arrays.asList(group, lines[0]));
        }
        npc.teleport(location, PlayerTeleportEvent.TeleportCause.PLUGIN);
        npc.setName("");
        return npc;
    }

    /**
     * Spawn all the CmdJoin-NPCs
     */
    public static void spawnNPCs() {
        if (!isCitizensSupport()) return;
        List<String> npcLocs = BedWars.config.getYml().getStringList(ConfigPath.GENERAL_CONFIGURATION_NPC_LOC_STORAGE);
        if (npcLocs == null) return;
        for (String s : npcLocs) {
            String[] data = s.split(",");
            if (data.length < 10) continue;
            if (!Misc.isNumber(data[0]) || !Misc.isNumber(data[1]) || !Misc.isNumber(data[2]) ||
                !Misc.isNumber(data[3]) || !Misc.isNumber(data[4]) || !Misc.isNumber(data[9])) continue;
            Location location = new Location(Bukkit.getWorld(data[5]),
                    Double.parseDouble(data[0]),
                    Double.parseDouble(data[1]),
                    Double.parseDouble(data[2]),
                    Float.parseFloat(data[3]),
                    Float.parseFloat(data[4]));
            String skin = data[6], name = data[7], group = data[8];
            int id = Integer.parseInt(data[9]);
            NPC npc = CitizensAPI.getNPCRegistry().getById(id);
            if (npc == null) {
                BedWars.plugin.getLogger().severe("Invalid npc id: " + id);
                continue;
            }
            spawnNPC(location, name, group, skin, npc);
        }
    }

    /**
     * Update CmdJoin NPCs for a group
     *
     * @param group arena group
     */
    public static void updateNPCs(String group) {
        String playerCount = String.valueOf(Arena.getPlayers(group));
        for (Map.Entry<ArmorStand, List<String>> entry : npcsHolograms.entrySet()) {
            List<String> value = entry.getValue();
            if (value.get(0).equalsIgnoreCase(group)) {
                ArmorStand stand = entry.getKey();
                if (stand != null && !stand.isDead()) {
                    stand.setCustomName(ChatColor.translateAlternateColorCodes('&', value.get(1).replace("{players}", playerCount)));
                }
            }
        }
    }
}
