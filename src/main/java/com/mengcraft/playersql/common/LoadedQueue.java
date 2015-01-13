package com.mengcraft.playersql.common;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.mengcraft.playersql.LoadedData;

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
