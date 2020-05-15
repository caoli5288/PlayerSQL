package com.mengcraft.playersql.event;

import lombok.Data;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.sql.Timestamp;

@Data
public class PlayerDataLockedEvent extends Event {

    public static final HandlerList HANDLER_LIST = new HandlerList();
    private final Timestamp lastUpdate;
    private final Player who;
    private Event.Result result = Event.Result.DEFAULT;

    public PlayerDataLockedEvent(Player who, Timestamp lastUpdate) {
        super(true);
        this.who = who;
        this.lastUpdate = lastUpdate;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
