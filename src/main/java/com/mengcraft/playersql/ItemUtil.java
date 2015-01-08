package com.mengcraft.playersql;

import org.bukkit.inventory.ItemStack;

public interface ItemUtil {
	public ItemStack getItemStack(String data) throws Exception;

	public String getString(ItemStack stack) throws Exception;
}
