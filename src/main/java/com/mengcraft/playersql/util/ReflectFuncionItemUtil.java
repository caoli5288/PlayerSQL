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

import org.bukkit.inventory.ItemStack;
import org.ow2.util.base64.Base64;

import com.mengcraft.playersql.ItemUtil;

public class ReflectFuncionItemUtil implements ItemUtil {
	private final static ReflectFuncionItemUtil UTIL = new ReflectFuncionItemUtil();

	private final Class<?> cachedItemStack;
	private final Class<?> cachedNBTTagCompound;
	private final Class<?> cachedCraftStack;
	private final Class<?> cachedNBTReadLimiter;
	private final Object cachedNBTReadUnlimited;
	private final Method cachedNBTTagWrite;
	private final Method cachedNBTTagLoad;
	private final Method cachedItemStackCreate;
	private final Method cachedItemStackSave;
	private final Method cachedCraftItemConversion;
	private final Method cachedCraftItemMirror;
	private final Field cachedCraftItemHandle;

	private ReflectFuncionItemUtil() {
		this.cachedItemStack = getItemStackClass();
		this.cachedNBTTagCompound = getNBTTagCompoundClass();
		this.cachedNBTReadLimiter = initNBTReadLimiterClass();
		this.cachedNBTReadUnlimited = getNBTReadLimiterObject();
		this.cachedNBTTagWrite = getNBTTagWriteMethod();
		this.cachedNBTTagLoad = getNBTTagLoadMethod();
		this.cachedItemStackCreate = getItemStackCreateMethod();
		this.cachedCraftStack = getCraftStack();
		this.cachedCraftItemConversion = getCraftItemConversionMethod();
		this.cachedCraftItemHandle = getCraftItemHandle();
		this.cachedCraftItemMirror = getCraftItemMirror();
		this.cachedItemStackSave = getItemStackSave();
	}

	private Object getNBTReadLimiterObject() {
		try {
			Field field = this.cachedNBTReadLimiter.getDeclaredField("a");
			field.setAccessible(true);
			Object object = field.get(this.cachedNBTReadLimiter);
			return object;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private Class<?> initNBTReadLimiterClass() {
		try {
			return Class.forName("net.minecraft.server.v1_8_R1.NBTReadLimiter");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Method getItemStackSave() {
		try {
			Method method = this.cachedItemStack.getMethod("save", this.cachedNBTTagCompound);
			return method;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Method getCraftItemMirror() {
		try {
			Method method = this.cachedCraftStack.getDeclaredMethod("asCraftMirror", this.cachedItemStack);
			return method;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Field getCraftItemHandle() {
		try {
			Field handle = this.cachedCraftStack.getDeclaredField("handle");
			handle.setAccessible(true);
			return handle;
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Method getCraftItemConversionMethod() {
		try {
			Method c = this.cachedCraftStack.getMethod("asCraftCopy", ItemStack.class);
			return c;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Class<?> getCraftStack() {
		try {
			Class<?> c = Class.forName("org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack");
			return c;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Method getItemStackCreateMethod() {
		try {
			Method create = this.cachedItemStack.getMethod("createStack", this.cachedNBTTagCompound);
			return create;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private Method getNBTTagLoadMethod() {
		try {
			Method load = this.cachedNBTTagCompound.getDeclaredMethod("load", new Class[] { DataInput.class, int.class, this.cachedNBTReadLimiter });
			load.setAccessible(true);
			return load;
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Method getNBTTagWriteMethod() {
		try {
			Method write = this.cachedNBTTagCompound.getDeclaredMethod("write", DataOutput.class);
			write.setAccessible(true);
			return write;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private Class<?> getNBTTagCompoundClass() {
		try {
			Class<?> c = Class.forName("net.minecraft.server.v1_8_R1.NBTTagCompound");
			return c;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Class<?> getItemStackClass() {
		try {
			Class<?> c = Class.forName("net.minecraft.server.v1_8_R1.ItemStack");
			return c;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public ItemStack getItemStack(String string) throws Exception {
		DataInput input = new DataInputStream(new ByteArrayInputStream(Base64.decode(string.toCharArray())));
		Object tag = this.cachedNBTTagCompound.newInstance();
		this.cachedNBTTagLoad.invoke(tag, new Object[] { input, 0, this.cachedNBTReadUnlimited });
		Object handle = this.cachedItemStackCreate.invoke(this.cachedItemStack, tag);
		Object stack = this.cachedCraftItemMirror.invoke(this.cachedCraftStack, handle);
		return (ItemStack) stack;
	}

	@Override
	public String getString(ItemStack stack) throws Exception {
		Object object = stack;
		if (stack instanceof ItemStack) {
			object = getAsCraftCopy(stack);
		}
		Object tag = this.cachedNBTTagCompound.newInstance();
		this.cachedItemStackSave.invoke(this.cachedCraftItemHandle.get(object), tag);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		this.cachedNBTTagWrite.invoke(tag, new DataOutputStream(out));
		return new String(Base64.encode(out.toByteArray()));
	}

	private Object getAsCraftCopy(ItemStack stack) {
		try {
			return this.cachedCraftItemConversion.invoke(this.cachedCraftStack, stack);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ReflectFuncionItemUtil getUtil() {
		return UTIL;
	}
}
