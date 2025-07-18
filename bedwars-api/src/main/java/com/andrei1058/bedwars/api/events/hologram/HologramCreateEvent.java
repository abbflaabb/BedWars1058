package com.andrei1058.bedwars.api.events.hologram;

import com.andrei1058.bedwars.api.hologram.IHologram;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class HologramCreateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final IHologram hologram;

    public HologramCreateEvent(IHologram hologram) {
        this.hologram = hologram;
    }

    public IHologram getHologram() {
        return hologram;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}