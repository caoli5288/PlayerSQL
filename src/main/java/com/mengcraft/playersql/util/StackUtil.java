package com.mengcraft.playersql.util;

import org.bukkit.inventory.ItemStack;
import org.ow2.util.base64.Base64;

public class StackUtil {
	public String getString(ItemStack stack) {
		Object origin = MinecraftReflect.get().getOriginItem(stack);
		Object tags = MinecraftReflect.get().getNBTTags(origin);
		byte[] bytes = MinecraftReflect.get().getNBTTagsBytes(tags);
		return encode(bytes);
	}

	public ItemStack getItemStack(String string) {
		byte[] bytes = decode(string);
		Object tags = MinecraftReflect.get().getNBTTags(bytes);
		Object origin = MinecraftReflect.get().getOriginItem(tags);
		Object item = MinecraftReflect.get().getCraftItem(origin);
		return (ItemStack) item;
	}

	private String encode(byte[] bs) {
		return new String(Base64.encode(bs));
	}

	private byte[] decode(String base64) {
		return Base64.decode(base64.toCharArray());
	}

}
