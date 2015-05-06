package com.mengcraft.playersql.task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mengcraft.playersql.LoadedData;
import com.mengcraft.playersql.LoadedQueue;
import com.mengcraft.playersql.LockedPlayer;
import com.mengcraft.playersql.util.FixedExp;
import com.mengcraft.playersql.util.ItemUtil;

public class CheckLoadedTask implements Runnable {

    private final ItemUtil util = ItemUtil.DEFAULT;

    private final Plugin plugin;
    private final Queue<LoadedData> handle;
    private final List<UUID> lockeds;

    @Override
    public void run() {
        while (this.handle.size() > 0) {
            work(this.handle.poll());
        }
    }

    private void work(LoadedData poll) {
        UUID key = poll.getKey();
        Player p = this.plugin.getServer().getPlayer(key);
        if (p != null) {
            sync(p, poll.getValue());
        }
        this.lockeds.remove(key);
    }

    private void sync(Player player, JsonArray value) {
        // TODO maybe need caching configure
        if (this.plugin.getConfig().getBoolean("sync.health")) {
            double health = value.get(0).getAsDouble();
            if (health > 20)
                health = 20;
            player.setHealth(health);
        }
        if (this.plugin.getConfig().getBoolean("sync.food")) {
            player.setFoodLevel(value.get(1).getAsInt());
        }
        if (this.plugin.getConfig().getBoolean("sync.exp")) {
            FixedExp.setExp(player, value.get(2).getAsInt());
        }
        if (this.plugin.getConfig().getBoolean("sync.inventory")) {
            player.getInventory().setContents(arrayToStacks(value.get(3).getAsJsonArray()));
            player.getInventory().setArmorContents(arrayToStacks(value.get(4).getAsJsonArray()));
            try {
                int hold = value.get(7).getAsInt();
                player.getInventory().setHeldItemSlot(hold);
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }
        if (this.plugin.getConfig().getBoolean("sync.chest")) {
            player.getEnderChest().setContents(arrayToStacks(value.get(5).getAsJsonArray()));
        }
        if (this.plugin.getConfig().getBoolean("sync.potion")) {
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
            player.addPotionEffects(arrayToEffects(value.get(6).getAsJsonArray()));
        }

    }

    public Collection<PotionEffect> arrayToEffects(JsonArray effects) {
        List<PotionEffect> effectList = new ArrayList<PotionEffect>();
        for (JsonElement element : effects) {
            JsonArray array = element.getAsJsonArray();
            String i = array.get(0).getAsString();
            int j = array.get(1).getAsInt();
            effectList.add(new PotionEffect(PotionEffectType.getByName(i), j, array.get(2).getAsInt(), array.get(3)
                    .getAsBoolean()));
        }
        return effectList;
    }

    public ItemStack[] arrayToStacks(JsonArray array) {
        List<ItemStack> stackList = new ArrayList<ItemStack>();
        for (JsonElement element : array) {
            if (element.isJsonNull()) {
                stackList.add(new ItemStack(Material.AIR));
            } else {
                stackList.add(this.util.getItemStack(element.getAsString()));
            }
        }
        return stackList.toArray(new ItemStack[array.size()]);
    }

    public CheckLoadedTask(Plugin plugin) {
        this.plugin = plugin;
        this.handle = LoadedQueue.getDefault().getHandle();
        this.lockeds = LockedPlayer.getDefault().getHandle();
    }
}
