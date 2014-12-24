package com.mengcraft.playersql.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MinecraftField {
	private final static MinecraftField FIELD = new MinecraftField();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Map<String, Field> map = new HashMap<>();

	private MinecraftField() {
	}

	public static MinecraftField get() {
		return FIELD;
	}

	/**
	 * Origin ItemStack handle in CraftItem
	 * 
	 * @return
	 */
	public Field getItemHandle() {
		ReadWriteLock lock = this.lock;
		lock.readLock().lock();
		Field field = this.map.get("ItemHandle");
		lock.readLock().unlock();
		if (field != null) {
			return field;
		} else {
			lock.writeLock().lock();
			initItemHandle();
			lock.writeLock().unlock();
			return getItemHandle();
		}
	}

	public Field getNBTReadLimiterUnlimited() {
		ReadWriteLock lock = this.lock;
		lock.readLock().lock();
		Field field = this.map.get("NBTReadLimiterUnlimited");
		lock.readLock().unlock();
		if (field != null) {
			return field;
		} else {
			lock.writeLock().lock();
			initNBTReadLimiterUnlimited();
			lock.writeLock().unlock();
			return getNBTReadLimiterUnlimited();
		}
	}

	private boolean initNBTReadLimiterUnlimited() {
		// TODO Auto-generated method stub
		Field field = this.map.get("NBTReadLimiterUnlimited");
		if (field != null) {
			return true;
		}
		Class<? extends Object> c = MinecraftClass.get().getNBTReadLimiter();
		try {
			field = c.getDeclaredField("a");
			this.map.put("NBTReadLimiterUnlimited", field);
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	private boolean initItemHandle() {
		Field field = this.map.get("ItemHandle");
		if (field != null) {
			return false;
		}
		Class<? extends Object> c = MinecraftClass.get().getCraftItem();
		try {
			field = c.getDeclaredField("handle");
			field.setAccessible(true);
			this.map.put("ItemHandle", field);
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return false;
	}
}
