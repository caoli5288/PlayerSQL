package com.mengcraft.playersql.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class FixedPlayerExp {
	private final static String VERSION = Bukkit.getServer().getClass().getName().split("\\.")[3];
	private final static FixedPlayerExp FIXED_PLAYER_EXP = new FixedPlayerExp();
	private final Class<?> c;
	private final Method g;
	private final Method s;

	private FixedPlayerExp() {
		this.c = getCompatibleClass();
		this.s = setMethod();
		this.g = getMethod();
	}

	public void set(Player player, int number) {
		try {
			this.s.invoke(this.c, new Object[] { player, number });
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public int get(Player player) {
		int i = 0;
		try {
			i = (int) this.g.invoke(this.c, player);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		return i;
	}

	private Method getMethod() {
		Method m = null;
		try {
			m = this.c.getMethod("getTotalExperience", Player.class);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return m;
	}

	private Method setMethod() {
		Method m = null;
		try {
			m = this.c.getMethod("setTotalExperience", new Class[] { Player.class, int.class });
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		return m;
	}

	private Class<?> getCompatibleClass() {
		Class<?> com = null;
		try {
			com = Class.forName("com.mengcraft.playersql.lib." + VERSION + ".SetExpFix");
		} catch (ClassNotFoundException e) {
			com = getDefaultClass();
		}
		return com;
	}

	private Class<?> getDefaultClass() {
		Class<?> def = null;
		try {
			def = Class.forName("com.mengcraft.playersql.lib.v1_6_R3.SetExpFix");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return def;
	}

	public static FixedPlayerExp getDefault() {
		return FIXED_PLAYER_EXP;
	}

}
