package com.andrei1058.bedwars.arena.Hologram;

import com.andrei1058.bedwars.api.hologram.IHologram;
import com.andrei1058.bedwars.api.hologram.IHologramManager;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class HologramManager implements IHologramManager {

    private final List<IHologram> holograms = new CopyOnWriteArrayList<>();

    @Override
    public void registerHologram(IHologram hologram) {
        if (!holograms.contains(hologram)) {
            holograms.add(hologram);
        }
    }

    @Override
    public void unregisterHologram(IHologram hologram) {
        holograms.remove(hologram);
    }

    @Override
    public List<IHologram> getAllHolograms() {
        return new ArrayList<>(holograms);
    }

    @Override
    public List<IHologram> getHologramsInRadius(Location center, double radius) {
        List<IHologram> nearby = new ArrayList<>();
        for (IHologram hologram : holograms) {
            if (hologram.getLocation().getWorld().equals(center.getWorld()) &&
                    hologram.getLocation().distance(center) <= radius) {
                nearby.add(hologram);
            }
        }
        return nearby;
    }

    @Override
    public void removeAllHolograms() {
        List<IHologram> toRemove = new ArrayList<>(holograms);
        for (IHologram hologram : toRemove) {
            hologram.remove();
        }
        holograms.clear();
    }

    @Override
    public IHologram createHologram(Location location) {
        return new Hologram(location);
    }
}