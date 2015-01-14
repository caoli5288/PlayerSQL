package com.mengcraft.playersql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class LockedPlayer {

	private final static LockedPlayer MANAGER = new LockedPlayer();
	private final List<UUID> handle;

	private LockedPlayer() {
		this.handle = Collections.synchronizedList(new ArrayList<UUID>());
	}

	public List<UUID> getHandle() {
		return handle;
	}

	public static LockedPlayer getDefault() {
		return MANAGER;
	}

}
