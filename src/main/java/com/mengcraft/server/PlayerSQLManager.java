package com.mengcraft.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerSQLManager {
	private final static PlayerSQLManager MANAGER = new PlayerSQLManager();
	private final List<String> list;

	private PlayerSQLManager() {
		this.list = Collections.synchronizedList(new ArrayList<String>());
	}
	
	public boolean isFrozen(String name) {
		return getList().contains(name);
	}
	
	public boolean setFrozen(String name, boolean frozen) {
		if (frozen) {
			return getList().add(name);
		} else {
			return getList().remove(name);
		}
	}

	private List<String> getList() {
		return list;
	}

	public static PlayerSQLManager getManager() {
		return MANAGER;
	}
}
