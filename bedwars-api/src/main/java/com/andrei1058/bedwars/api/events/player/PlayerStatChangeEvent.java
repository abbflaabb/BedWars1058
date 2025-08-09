package com.andrei1058.bedwars.api.events.player;

import com.andrei1058.bedwars.api.arena.IArena;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * An event that is called when player statistics are changed.
 */
public class PlayerStatChangeEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private Player player;
    private IArena arena;
    private StatType statType;
    private boolean cancelled = false;

    /**
     * Constructs a new PlayerStatChangeEvent.
     *
     * @param player   the target player
     * @param arena    the target arena
     * @param statType the type of statistic being changed
     */
    public PlayerStatChangeEvent(Player player, IArena arena, StatType statType) {
        this.player = player;
        this.arena = arena;
        this.statType = statType;
    }

    /**
     * Gets the arena associated with the event.
     *
     * @return the arena associated with the event
     */
    public IArena getArena() {
        return arena;
    }

    /**
     * Gets the player associated with the event.
     *
     * @return the player associated with the event
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Gets the type of statistic being changed.
     *
     * @return the type of statistic being changed
     */
    public StatType getStatType() {
        return statType;
    }

    /**
     * Gets the list of event handlers for the event.
     *
     * @return the list of event handlers for the event
     */
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    /**
     * Gets the list of event handlers for the event.
     *
     * @return the list of event handlers for the event
     */
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Checks if the event is cancelled.
     *
     * @return true if the event is cancelled, false otherwise
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets the cancellation status of the event.
     *
     * @param cancelled true to cancel the event, false otherwise
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    /**
     * Represents the types of statistics that can be changed.
     */
    public enum StatType {
        FIRST_PLAY,
        LAST_PLAY,
        WINS,
        KILLS,
        FINAL_KILLS,
        LOSSES,
        DEATHS,
        FINAL_DEATHS,
        BEDS_DESTROYED,
        GAMES_PLAYED,
        CUSTOM
    }
}