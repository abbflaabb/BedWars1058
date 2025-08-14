package com.andrei1058.bedwars.support.citizens;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.language.Messages;
import com.andrei1058.bedwars.arena.Arena;
import com.andrei1058.bedwars.arena.Misc;
import com.andrei1058.bedwars.configuration.Sounds;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.ArrayList;
import java.util.List;

import static com.andrei1058.bedwars.api.language.Language.getMsg;

public class CitizensListener implements Listener {

    @EventHandler
    public void removeNPC(NPCRemoveEvent event) {
        if (event == null || event.getNPC() == null || event.getNPC().getEntity() == null) return;
        List<String> locations = BedWars.config.getYml().getStringList(ConfigPath.GENERAL_CONFIGURATION_NPC_LOC_STORAGE);
        boolean removed = false;
        int npcId = event.getNPC().getId();
        if (JoinNPC.npcs.containsKey(npcId)) {
            JoinNPC.npcs.remove(npcId);
            removed = true;
        }
        for (String s : new ArrayList<>(locations)) {
            String[] data = s.split(",");
            if (data.length >= 10 && Misc.isNumber(data[9]) && Integer.parseInt(data[9]) == npcId) {
                locations.remove(s);
                removed = true;
            }
        }
        for (Entity entity : event.getNPC().getEntity().getNearbyEntities(0, 3, 0)) {
            if (entity.getType() == EntityType.ARMOR_STAND && entity.hasMetadata("bw-npc")) {
                entity.remove();
            }
        }
        if (removed) {
            BedWars.config.set(ConfigPath.GENERAL_CONFIGURATION_NPC_LOC_STORAGE, locations);
        }
    }

    @EventHandler
    // Citizens support
    public void onNPCInteract(PlayerInteractEntityEvent event) {
        if (!JoinNPC.isCitizensSupport()) return;
        if (event.getPlayer().isSneaking()) return;
        if (!event.getRightClicked().hasMetadata("NPC")) return;
        net.citizensnpcs.api.npc.NPC npc = CitizensAPI.getNPCRegistry().getNPC(event.getRightClicked());
        if (npc == null) return;
        String groupId = JoinNPC.npcs.get(npc.getId());
        if (groupId != null) {
            if (!Arena.joinRandomFromGroup(event.getPlayer(), groupId)) {
                event.getPlayer().sendMessage(getMsg(event.getPlayer(), Messages.COMMAND_JOIN_NO_EMPTY_FOUND));
                Sounds.playSound("join-denied", event.getPlayer());
            } else {
                Sounds.playSound("join-allowed", event.getPlayer());
            }
        }
    }

}
