package com.mengcraft.playersql.task;

import java.util.Queue;
import java.util.UUID;

import com.mengcraft.playersql.LoadTaskQueue;
import com.mengcraft.playersql.TaskManager;

public class CheckQueuedTask implements Runnable {

	private final Queue<UUID> queue;

	private final TaskManager manager = TaskManager.getManager();

	@Override
	public void run() {
		if (this.queue.size() > 0) {
			work(this.queue.poll());
		}
	}

	private void work(UUID poll) {
		this.manager.runLoadTask(poll);
	}

	public CheckQueuedTask() {
		this.queue = LoadTaskQueue.getManager().getHandle();
	}

}
