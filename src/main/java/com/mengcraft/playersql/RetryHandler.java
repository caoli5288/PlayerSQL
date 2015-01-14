package com.mengcraft.playersql;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RetryHandler {

	private final static RetryHandler HANDLER = new RetryHandler();

	private final Map<UUID, Integer> map;

	private RetryHandler() {
		this.map = new ConcurrentHashMap<>();
	}

	public static RetryHandler getHandler() {
		return HANDLER;
	}

	public Map<UUID, Integer> getMap() {
		return map;
	}

	/**
	 * Return true if reach max retry number.
	 * 
	 * @param uuid
	 * @return
	 */
	public boolean check(UUID uuid) {
		if (this.map.containsKey(uuid)) {
			return checkNumber(uuid) > 3;
		}
		this.map.put(uuid, 1);
		return false;
	}

	private int checkNumber(UUID uuid) {
		int i = this.map.get(uuid) + 1;
		if (i > 3) {
			this.map.remove(uuid);
		} else {
			this.map.put(uuid, i);
		}
		return i;
	}

}
