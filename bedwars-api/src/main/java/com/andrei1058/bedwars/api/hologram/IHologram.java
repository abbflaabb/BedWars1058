package com.andrei1058.bedwars.api.hologram;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

import java.util.List;

public interface IHologram {
    void addLine(Location loc, String text);
    void createResourceChestHologram(Location loc);
    void remove();
    List<ArmorStand> getLines();
    boolean isRemoved();
    Location getLocation();
    void updateLine(int index, String newText);
}
