package com.mengcraft.playersql.event;

import lombok.Data;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Called when a player's data processed even if any exception thrown.
 */
@Data
public class PlayerDataProcessedEvent extends PlayerEvent {

    public static final HandlerList HANDLER_LIST = new HandlerList();
    private final Exception exception;

    private PlayerDataProcessedEvent(Player who, Exception exception) {
        super(who);
        this.exception = exception;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public static PlayerDataProcessedEvent call(Player who, Exception exception) {
        val evt = new PlayerDataProcessedEvent(who, exception);
        Bukkit.getPluginManager().callEvent(evt);
        return evt;
    }

}
