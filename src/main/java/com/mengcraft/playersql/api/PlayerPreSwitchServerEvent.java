package com.mengcraft.playersql.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerPreSwitchServerEvent extends Event implements Cancellable {

	private static HandlerList handlers = new HandlerList();

	private String target;
	private boolean cancelled;

	private final Player player;

	public PlayerPreSwitchServerEvent(Player player, String target) {
		if (player == null || target == null) {
			throw new NullPointerException("Argument canot be null!");
		}
		this.player = player;
		this.target = target;
	}

	public Player getPlayer() {
		return player;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		if (target == null) {
			throw new NullPointerException("Target can not be null!");
		}
		this.target = target;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

}
