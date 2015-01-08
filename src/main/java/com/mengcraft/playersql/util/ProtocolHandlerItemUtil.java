package com.mengcraft.playersql.util;

import java.io.IOException;

import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.utility.StreamSerializer;
import com.mengcraft.playersql.ItemUtil;

public class ProtocolHandlerItemUtil implements ItemUtil {
	private final StreamSerializer serializer = StreamSerializer.getDefault();

	@Override
	public ItemStack getItemStack(String data) throws IOException {
		return this.serializer.deserializeItemStack(data);
	}

	@Override
	public String getString(ItemStack stack) throws IOException {
		return this.serializer.serializeItemStack(stack);
	}

}
