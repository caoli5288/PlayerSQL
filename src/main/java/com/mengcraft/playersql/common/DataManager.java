package com.mengcraft.playersql.common;

import com.avaje.ebeaninternal.server.lib.sql.DataSourceManager;

public class DataManager {

	private final static DataManager MANAGER = new DataManager();
	private final DataSourceManager handle = new DataSourceManager();

	public DataSourceManager getHandle() {
		return handle;
	}

	public static DataManager getDefault() {
		return MANAGER;
	}

}
