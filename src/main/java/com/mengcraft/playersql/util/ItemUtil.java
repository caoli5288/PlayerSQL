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

import net.minecraft.server.v1_8_R2.ItemStack;
import net.minecraft.server.v1_8_R2.NBTReadLimiter;
import net.minecraft.server.v1_8_R2.NBTTagCompound;

import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack;
import org.ow2.util.base64.Base64;

public class ItemUtil {

	private final static ItemUtil UTIL = new ItemUtil();

	private final Method cachedNBTTagWrite;
	private final Method cachedNBTTagLoad;
	private final Method cachedItemStackSave;
	private final Field cachedCraftItemHandle;

	private ItemUtil() {
		this.cachedNBTTagWrite = getNBTTagWriteMethod();
		this.cachedNBTTagLoad = getNBTTagLoadMethod();
		this.cachedCraftItemHandle = getCraftItemHandle();
		this.cachedItemStackSave = getItemStackSave();
	}

	private Method getItemStackSave() {
		try {
			Method method = net.minecraft.server.v1_8_R2.ItemStack.class.getMethod("save", NBTTagCompound.class);
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
			Field handle = CraftItemStack.class.getDeclaredField("handle");
			handle.setAccessible(true);
			return handle;
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Method getNBTTagLoadMethod() {
		try {
			Method load = NBTTagCompound.class.getDeclaredMethod("load", new Class[] { DataInput.class, int.class, NBTReadLimiter.class });
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
			Method write = NBTTagCompound.class.getDeclaredMethod("write", DataOutput.class);
			write.setAccessible(true);
			return write;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public CraftItemStack getItemStack(String string) {
		DataInput input = new DataInputStream(new ByteArrayInputStream(Base64.decode(string.toCharArray())));
		NBTTagCompound tag = new NBTTagCompound();
		try {
			this.cachedNBTTagLoad.invoke(tag, new Object[] { input, 0, NBTReadLimiter.a });
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		ItemStack handle = ItemStack.createStack(tag);
		return CraftItemStack.asCraftMirror(handle);
	}

	public String getString(CraftItemStack stack) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			NBTTagCompound tag = new NBTTagCompound();
			this.cachedItemStackSave.invoke(this.cachedCraftItemHandle.get(stack), tag);
			this.cachedNBTTagWrite.invoke(tag, new DataOutputStream(out));
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return new String(Base64.encode(out.toByteArray()));
	}

	public static ItemUtil getUtil() {
		return UTIL;
	}

}
