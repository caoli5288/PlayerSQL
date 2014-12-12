package com.mengcraft.playersql;

public class TimerTask implements Runnable {
	@Override
	public void run() {
		TaskManaget.getManaget().saveAllTask(false);
	}
}
