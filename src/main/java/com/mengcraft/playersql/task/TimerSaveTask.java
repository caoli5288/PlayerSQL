package com.mengcraft.playersql.task;

import org.bukkit.plugin.Plugin;

import com.mengcraft.playersql.TaskManager;

public class TimerSaveTask implements Runnable {

	private final TaskManager manager = TaskManager.getManager();
	private final Plugin plugin;

	@Override
	public void run() {
		this.manager.runSaveAll(plugin, 1);
	}

	public TimerSaveTask(Plugin plugin) {
		this.plugin = plugin;
	}

}
