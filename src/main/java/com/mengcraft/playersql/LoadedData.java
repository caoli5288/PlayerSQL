package com.mengcraft.playersql;

import java.util.Map.Entry;
import java.util.UUID;

import com.google.gson.JsonArray;

public class LoadedData implements Entry<UUID, JsonArray> {

	private final UUID key;
	private JsonArray value;

	@Override
	public UUID getKey() {
		return this.key;
	}

	@Override
	public JsonArray getValue() {
		return this.value;
	}

	@Override
	public JsonArray setValue(JsonArray value) {
		JsonArray old = this.value;
		this.value = value;
		return old;
	}

	public LoadedData(UUID key, JsonArray value) {
		this.key = key;
		this.value = value;
	}

}
