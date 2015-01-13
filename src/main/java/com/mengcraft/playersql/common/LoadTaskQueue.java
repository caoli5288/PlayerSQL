package com.mengcraft.playersql.common;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LoadTaskQueue {

	private final static LoadTaskQueue MANAGER = new LoadTaskQueue();
	private final Queue<UUID> handle = new ConcurrentLinkedQueue<>();

	public static LoadTaskQueue getManager() {
		return MANAGER;
	}

	public Queue<UUID> getHandle() {
		return this.handle;
	}
}
