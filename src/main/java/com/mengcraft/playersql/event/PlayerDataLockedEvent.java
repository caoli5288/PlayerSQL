package com.mengcraft.playersql.event;

import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import java.sql.Timestamp;

@Data
public class PlayerDataLockedEvent extends PlayerEvent {

    public static final HandlerList HANDLER_LIST = new HandlerList();
    private final Timestamp lastUpdate;
    private Result result = Result.DEFAULT;

    public PlayerDataLockedEvent(Player who, Timestamp lastUpdate) {
        super(who);
        this.lastUpdate = lastUpdate;
    }

    @Override
    public HandlerList getHandlers() {
        return null;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
