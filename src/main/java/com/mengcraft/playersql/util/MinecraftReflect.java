package com.mengcraft.playersql.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MinecraftReflect {
	private final static MinecraftReflect REFLECT = new MinecraftReflect();
	private Object readLimiter;

	private MinecraftReflect() {
	}

	public static MinecraftReflect get() {
		return REFLECT;
	}

	/**
	 * Get origin ItemStack from NBTTags.
	 * 
	 * @param tags
	 * @return
	 */
	public Object getOriginItem(Object tags) {
		Class<? extends Object> item = MinecraftClass.get().getOriginItem();
		Method method = MinecraftMethod.get().getCreateStack();
		Object object = null;
		try {
			object = method.invoke(item, tags);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return object;
	}

	/**
	 * Get origin ItemStack from bukkit ItemStack.
	 * 
	 * @param stack
	 * @return
	 */
	public Object getOriginItem(ItemStack stack) {
		Object object = getCraftItem(stack);
		Field field = MinecraftField.get().getItemHandle();
		Object origin = null;
		try {
			origin = field.get(object);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return origin;
	}

	/**
	 * Get NBTTags from bytes.
	 * 
	 * @param bytes
	 * @return
	 */
	public Object getNBTTags(byte[] bytes) {
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		DataInput input = new DataInputStream(in);
		Class<? extends Object> c = MinecraftClass.get().getNBTTags();
		Method method = MinecraftMethod.get().getNBTTagsLoad();
		Object object = null;
		try {
			object = c.newInstance();
			// Must be this.readLimiter not getNBTReadLimiter()
			if (this.readLimiter != null) {
				method.invoke(object, new Object[] { input, 0, this.readLimiter });
			} else {
				method.invoke(object, new Object[] { input, 0 });
			}
		} catch (IllegalArgumentException e) {
			magic(object, input);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return object;
	}

	private void magic(Object object, DataInput input) {
		Method method = MinecraftMethod.get().getNBTTagsLoad();
		try {
			method.invoke(object, new Object[] { input, 0, getNBTReadLimiter() });
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	private Object getNBTReadLimiter() {
		if (this.readLimiter != null) {
			return this.readLimiter;
		}
		Class<? extends Object> j = MinecraftClass.get().getNBTReadLimiter();
		Field field = MinecraftField.get().getNBTReadLimiterUnlimited();
		try {
			this.readLimiter = field.get(j);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return getNBTReadLimiter();
	}

	/**
	 * Get NBTTags from origin ItemStack.
	 * 
	 * @param object
	 * @return
	 */
	public Object getNBTTags(Object item) {
		if (item == null) {
			throw new NullPointerException("Item can not be null!");
		}
		Object tags = null;
		try {
			Method method = MinecraftMethod.get().getNBTTagsSave();
			tags = MinecraftClass.get().getNBTTags().newInstance();
			method.invoke(item, tags);
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return tags;
	}

	/**
	 * Get bytes from NBTTags.
	 * 
	 * @param origin
	 * @return
	 */
	public byte[] getNBTTagsBytes(Object tags) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutput output = new DataOutputStream(out);
		Method method = MinecraftMethod.get().getNBTTagsWrite();
		try {
			method.invoke(tags, output);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return out.toByteArray();
	}

	/**
	 * Get CraftItem from origin ItemStack.
	 * 
	 * @param origin
	 * @return
	 */
	public Object getCraftItem(Object origin) {
		Object stack = new ItemStack(Material.AIR);
		try {
			Method method = MinecraftMethod.get().getCraftMirror();
			Class<? extends Object> craft = MinecraftClass.get().getCraftItem();
			stack = method.invoke(craft, origin);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return stack;
	}

	/**
	 * Get CraftItem if param is a instance of Bukkit ItemStack
	 * 
	 * @param stack
	 * @return
	 */
	public Object getCraftItem(ItemStack stack) {
		Class<? extends Object> c = MinecraftClass.get().getCraftItem();
		if (c.isInstance(stack)) {
			return stack;
		}
		Method method = MinecraftMethod.get().getCraftCopy();
		Object object = null;
		try {
			object = method.invoke(c, stack);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return object;
	}
}
