package com.mengcraft.playersql.event;

import com.mengcraft.playersql.PlayerData;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

@Data
public class PlayerDataStoreEvent extends PlayerEvent {

    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final PlayerData data;

    public PlayerDataStoreEvent(Player who, PlayerData data) {
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

    public static PlayerDataStoreEvent call(Player player, PlayerData data) {
        PlayerDataStoreEvent event = new PlayerDataStoreEvent(player, data);
        Bukkit.getPluginManager().callEvent(event);
        return event;
    }
}
