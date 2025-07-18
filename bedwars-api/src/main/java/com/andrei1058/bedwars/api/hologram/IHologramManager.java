package com.andrei1058.bedwars.api.hologram;

import org.bukkit.Location;

import java.util.List;

public interface IHologramManager {
    void registerHologram(IHologram hologram);
    void unregisterHologram(IHologram hologram);
    List<IHologram> getAllHolograms();
    List<IHologram> getHologramsInRadius(Location center, double radius);
    void removeAllHolograms();
    IHologram createHologram(Location loc);
}
