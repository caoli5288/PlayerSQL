package com.mengcraft.playersql.event;

import com.mengcraft.playersql.PlayerData;
import lombok.Data;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

/**
 * Called when player's data fetched from database and be ready for processing.
 */
@Data
public class PlayerDataFetchedEvent extends PlayerEvent implements Cancellable {

    public static final HandlerList HANDLER_LIST = new HandlerList();
    private final PlayerData data;
    private boolean cancelled;

    private PlayerDataFetchedEvent(Player who, PlayerData data) {
        super(who);
        this.data = data;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    public static PlayerDataFetchedEvent call(Player who, PlayerData data) {
        val evt = new PlayerDataFetchedEvent(who, data);
        Bukkit.getPluginManager().callEvent(evt);
        return evt;
    }

}
