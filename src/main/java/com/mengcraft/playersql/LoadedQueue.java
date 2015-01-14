package com.mengcraft.playersql;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LoadedQueue {

	private final static LoadedQueue MANAGER = new LoadedQueue();
	private final Queue<LoadedData> handle;

	private LoadedQueue() {
		this.handle = new ConcurrentLinkedQueue<>();
	}

	public Queue<LoadedData> getHandle() {
		return handle;
	}

	public static LoadedQueue getDefault() {
		return MANAGER;
	}

}
