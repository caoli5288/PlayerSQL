package com.mengcraft.playersql.lib.v1_6_R3;

import org.bukkit.entity.Player;

public class SetExpFix {
	public static void setTotalExperience(Player player, int exp)
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

	private static int getExpAtLevel(Player player)
	{
		return getExpAtLevel(player.getLevel());
	}

	public static int getExpAtLevel(int level)
	{
		if (level > 29) { return 62 + (level - 30) * 7; }
		if (level > 15) { return 17 + (level - 15) * 3; }
		return 17;
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

	public static int getTotalExperience(Player player)
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

	public static int getExpUntilNextLevel(Player player)
	{
		int exp = Math.round(getExpAtLevel(player) * player.getExp());
		int nextLevel = player.getLevel();
		return getExpAtLevel(nextLevel) - exp;
	}
}
