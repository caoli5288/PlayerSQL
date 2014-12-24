package com.mengcraft.playersql.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.bukkit.Bukkit;

public class MinecraftClass {
	private final static MinecraftClass CLASS = new MinecraftClass();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Map<String, Class<? extends Object>> map = new HashMap<>();
	private final String version;

	private MinecraftClass() {
		this.version = Bukkit.getServer().getClass().getName().split("\\.")[3];
	}

	public static MinecraftClass get() {
		return CLASS;
	}

	public Class<? extends Object> getCraftItem() {
		ReadWriteLock lock = this.lock;
		lock.readLock().lock();
		Class<? extends Object> c = this.map.get("CraftItemStack");
		lock.readLock().unlock();
		if (c != null) {
			return c;
		} else {
			lock.writeLock().lock();
			initCraftItemStack();
			lock.writeLock().unlock();
			return getCraftItem();
		}
	}

	public Class<? extends Object> getOriginItem() {
		ReadWriteLock lock = this.lock;
		lock.readLock().lock();
		Class<? extends Object> c = this.map.get("ItemStack");
		lock.readLock().unlock();
		if (c != null) {
			return c;
		} else {
			lock.writeLock().lock();
			initItemStack();
			lock.writeLock().unlock();
			return getOriginItem();
		}
	}

	public Class<? extends Object> getNBTTags() {
		ReadWriteLock lock = this.lock;
		lock.readLock().lock();
		Class<? extends Object> c = this.map.get("NBTTagCompound");
		lock.readLock().unlock();
		if (c != null) {
			return c;
		} else {
			lock.writeLock().lock();
			initNBTTags();
			lock.writeLock().unlock();
			return getNBTTags();
		}
	}

	public Class<? extends Object> getNBTReadLimiter() {
		ReadWriteLock lock = this.lock;
		lock.readLock().lock();
		Class<? extends Object> c = this.map.get("NBTReadLimiter");
		lock.readLock().unlock();
		if (c != null) {
			return c;
		} else {
			lock.writeLock().lock();
			initNBTReadLimiter();
			lock.writeLock().unlock();
			return getNBTReadLimiter();
		}
	}

	private boolean initNBTReadLimiter() {
		Class<? extends Object> c = this.map.get("NBTReadLimiter");
		if (c != null) {
			return false;
		}
		try {
			c = Class.forName("net.minecraft.server." + this.getVersion() + ".NBTReadLimiter");
			this.map.put("NBTReadLimiter", c);
		} catch (ClassNotFoundException e) {
			return false;
		}
		return true;

	}

	private boolean initNBTTags() {
		Class<? extends Object> c = this.map.get("NBTTagCompound");
		if (c != null) {
			return false;
		}
		try {
			c = Class.forName("net.minecraft.server." + this.getVersion() + ".NBTTagCompound");
			this.map.put("NBTTagCompound", c);
		} catch (ClassNotFoundException e) {
			return false;
		}
		return true;
	}

	private boolean initItemStack() {
		Class<? extends Object> c = this.map.get("ItemStack");
		if (c != null) {
			return false;
		}
		try {
			c = Class.forName("net.minecraft.server." + this.getVersion() + ".ItemStack");
			this.map.put("ItemStack", c);
		} catch (ClassNotFoundException e) {
			return false;
		}
		return true;
	}

	private boolean initCraftItemStack() {
		Class<? extends Object> c = this.map.get("CraftItemStack");
		if (c != null) {
			return false;
		}
		try {
			c = Class.forName("org.bukkit.craftbukkit." + this.getVersion() + ".inventory.CraftItemStack");
			this.map.put("CraftItemStack", c);
		} catch (ClassNotFoundException e) {
			return false;
		}
		return true;
	}

	public String getVersion() {
		return version;
	}
}
