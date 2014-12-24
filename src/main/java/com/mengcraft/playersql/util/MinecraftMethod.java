package com.mengcraft.playersql.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.bukkit.inventory.ItemStack;

public class MinecraftMethod {
	private final static MinecraftMethod METHOD = new MinecraftMethod();
	private final Map<String, Method> map = new HashMap<>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	private MinecraftMethod() {
	}

	public static MinecraftMethod get() {
		return METHOD;
	}

	/**
	 * Get CraftItem from origin ItemStack.
	 * 
	 * @return
	 */
	public Method getCraftMirror() {
		ReadWriteLock lock = this.lock;
		lock.readLock().lock();
		Method method = this.map.get("CraftMirror");
		lock.readLock().unlock();
		if (method != null) {
			return method;
		} else {
			lock.writeLock().lock();
			initCraftMirror();
			lock.writeLock().unlock();
			return getCraftMirror();
		}
	}

	/**
	 * Get origin ItemStack from NBTTags.
	 * 
	 * @return
	 */
	public Method getCreateStack() {
		ReadWriteLock lock = this.lock;
		lock.readLock().lock();
		Method method = this.map.get("OriginStack");
		lock.readLock().unlock();
		if (method != null) {
			return method;
		} else {
			lock.writeLock().lock();
			initCreateStack();
			lock.writeLock().unlock();
			return getCreateStack();
		}
	}

	/**
	 * Get CraftItem from bukkit ItemStack.
	 * 
	 * @return
	 */
	public Method getCraftCopy() {
		ReadWriteLock lock = this.lock;
		lock.readLock().lock();
		Method method = this.map.get("CraftCopy");
		lock.readLock().unlock();
		if (method != null) {
			return method;
		} else {
			lock.writeLock().lock();
			initCraftCopy();
			lock.writeLock().unlock();
			return getCraftCopy();
		}

	}

	/**
	 * Get NBTTags from byte array.
	 * 
	 * @return
	 */
	public Method getNBTTagsLoad() {
		ReadWriteLock lock = this.lock;
		lock.readLock().lock();
		Method method = this.map.get("NBTTagsLoad");
		lock.readLock().unlock();
		if (method != null) {
			return method;
		} else {
			lock.writeLock().lock();
			initNBTTagsLoad();
			lock.writeLock().unlock();
			return getNBTTagsLoad();
		}
	}

	public Method getNBTTagsWrite() {
		ReadWriteLock lock = this.lock;
		lock.readLock().lock();
		Method method = this.map.get("NBTTagsWrite");
		lock.readLock().unlock();
		if (method != null) {
			return method;
		} else {
			lock.writeLock().lock();
			initNBTTagsWrite();
			lock.writeLock().unlock();
			return getNBTTagsWrite();
		}

	}

	public Method getNBTTagsSave() {
		ReadWriteLock lock = this.lock;
		lock.readLock().lock();
		Method method = this.map.get("NBTTagsSave");
		lock.readLock().unlock();
		if (method != null) {
			return method;
		} else {
			lock.writeLock().lock();
			initNBTTagsSave();
			lock.writeLock().unlock();
			return getNBTTagsSave();
		}
	}

	/**
	 * Get NBTTages from origin ItemStack.
	 * 
	 * @return
	 */
	private boolean initNBTTagsSave() {
		Method method = this.map.get("NBTTagsSave");
		if (method != null) {
			return true;
		}
		Class<? extends Object> c = MinecraftClass.get().getOriginItem();
		Class<? extends Object> b = MinecraftClass.get().getNBTTags();
		try {
			method = c.getMethod("save", b);
			this.map.put("NBTTagsSave", method);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return false;
	}

	private boolean initNBTTagsWrite() {
		Method method = this.map.get("NBTTagsWrite");
		if (method != null) {
			return false;
		}
		Class<? extends Object> c = MinecraftClass.get().getNBTTags();
		try {
			method = c.getDeclaredMethod("write", DataOutput.class);
			method.setAccessible(true);
			this.map.put("NBTTagsWrite", method);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Get NBTTags from bytes.
	 * 
	 * @return
	 */
	private boolean initNBTTagsLoad() {
		Method method = this.map.get("NBTTagsLoad");
		if (method != null) {
			return false;
		}
		Class<? extends Object> c = MinecraftClass.get().getNBTTags();
		try {
			if (MinecraftClass.get().getVersion().startsWith("v1_6")) {
				method = c.getDeclaredMethod("load", new Class[] { DataInput.class, int.class });
			} else {
				Class<? extends Object> j = MinecraftClass.get().getNBTReadLimiter();
				method = c.getDeclaredMethod("load", new Class[] { DataInput.class, int.class, j });
			}
			method.setAccessible(true);
			this.map.put("NBTTagsLoad", method);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return true;
	}

	private boolean initCraftCopy() {
		Method method = this.map.get("CraftCopy");
		if (method != null) {
			return false;
		}
		Class<? extends Object> craft = MinecraftClass.get().getCraftItem();
		try {
			method = craft.getMethod("asCraftCopy", ItemStack.class);
			this.map.put("CraftCopy", method);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	private boolean initCreateStack() {
		Method method = this.map.get("OriginStack");
		if (method != null) {
			return false;
		}
		Class<? extends Object> c = MinecraftClass.get().getOriginItem();
		Class<? extends Object> nbt = MinecraftClass.get().getNBTTags();
		try {
			method = c.getMethod("createStack", nbt);
			this.map.put("OriginStack", method);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return true;
	}

	private boolean initCraftMirror() {
		Method method = this.map.get("CraftMirror");
		if (method != null) {
			return false;
		}
		Class<? extends Object> craft = MinecraftClass.get().getCraftItem();
		Class<? extends Object> nms = MinecraftClass.get().getOriginItem();
		try {
			method = craft.getMethod("asCraftMirror", nms);
			this.map.put("CraftMirror", method);
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
