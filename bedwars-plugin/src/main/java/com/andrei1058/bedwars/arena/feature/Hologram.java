package com.andrei1058.bedwars.arena.feature;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

import java.util.ArrayList;
import java.util.List;

public class Hologram {

    private final List<ArmorStand> lines = new ArrayList<>();

    public Hologram(Location loc) {
    }

    public void addLine(Location loc, String text) {
        double spacing = BedWars.config.getDouble(ConfigPath.GENERAL_CONFIGURATION_RESOURCE_CHEST_HOLOGRAM_SPACING);
        Location lineLoc = loc.clone().add(0, -spacing * lines.size(), 0);

        ArmorStand stand = (ArmorStand) loc.getWorld().spawn(lineLoc, ArmorStand.class);
        stand.setCustomName(ChatColor.translateAlternateColorCodes('&', text));
        stand.setCustomNameVisible(true);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setSmall(true);

        lines.add(stand);
    }

    public void createResourceChestHologram(Location loc) {
        String titleText = BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_RESOURCE_CHEST_HOLOGRAM_TITLE);
        String subtitleText = BedWars.config.getString(ConfigPath.GENERAL_CONFIGURATION_RESOURCE_CHEST_HOLOGRAM_SUBTITLE);

        double yOffset = BedWars.config.getDouble(ConfigPath.GENERAL_CONFIGURATION_RESOURCE_CHEST_HOLOGRAM_Y_OFFSET);

        addLine(loc.clone().add(0.5, yOffset, 0.5), titleText);
        addLine(loc.clone().add(0.5, yOffset, 0.5), subtitleText);
    }

    public void remove() {
        lines.forEach(ArmorStand::remove);
        lines.clear();
    }

    public List<ArmorStand> getLines() {
        return new ArrayList<>(lines);
    }
}