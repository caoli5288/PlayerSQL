package com.mengcraft.playersql.util;

import org.bukkit.entity.Player;

public class FixedExp
{
	public static void setExp(Player player, int exp)
	{
		if (exp < 0) { throw new IllegalArgumentException("Experience is negative!"); }
		player.setExp(0.0F);
		player.setLevel(0);
		player.setTotalExperience(0);

		int amount = exp;
		while (amount > 0)
		{
			int expToLevel = getExpAtLevel(player);
			amount -= expToLevel;
			if (amount >= 0)
			{
				player.giveExp(expToLevel);
			}
			else
			{
				amount += expToLevel;
				player.giveExp(amount);
				amount = 0;
			}
		}
	}

	public static int getExp(Player player)
	{
		int exp = Math.round(getExpAtLevel(player) * player.getExp());
		int currentLevel = player.getLevel();
		while (currentLevel > 0)
		{
			currentLevel--;
			exp += getExpAtLevel(currentLevel);
		}
		if (exp < 0) {
			exp = 2147483647;
		}
		return exp;
	}

	private static int getExpAtLevel(Player player)
	{
		return getExpAtLevel(player.getLevel());
	}

	public static int getExpAtLevel(int level)
	{
		if (level <= 15) { return 2 * level + 7; }
		if ((level >= 16) && (level <= 30)) { return 5 * level - 38; }
		return 9 * level - 158;
	}

	public static int getExpToLevel(int level)
	{
		int currentLevel = 0;
		int exp = 0;
		while (currentLevel < level)
		{
			exp += getExpAtLevel(currentLevel);
			currentLevel++;
		}
		if (exp < 0) {
			exp = 2147483647;
		}
		return exp;
	}

	public static int getExpUntilNextLevel(Player player)
	{
		int exp = Math.round(getExpAtLevel(player) * player.getExp());
		int nextLevel = player.getLevel();
		return getExpAtLevel(nextLevel) - exp;
	}
}
