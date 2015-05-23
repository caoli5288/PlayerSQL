package com.mengcraft.playersql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mengcraft.playersql.task.LoadTask;
import com.mengcraft.playersql.task.SaveTask;
import com.mengcraft.playersql.task.UnlockTask;
import com.mengcraft.playersql.util.ExpsUtil;
import com.mengcraft.playersql.util.ItemUtil;
import com.mengcraft.util.ArrayBuilder;

public class SyncManager {

    public static final SyncManager DEFAULT = new SyncManager();

    private final ExecutorService service;
    private final JsonParser parser = new JsonParser();
    private final DataCompound compond = DataCompound.DEFAULT;

    private SyncManager() {
        this.service = new ThreadPoolExecutor(2, Integer.MAX_VALUE,
                60000,
                TimeUnit.MILLISECONDS,
                new SynchronousQueue<Runnable>()
                );
    }

    public void save(Player player, boolean unlock) {
        if (player == null) {
            throw new NullPointerException("#11 Can not save a null player.");
        }
        String data = data(player);
        UUID   uuid = player.getUniqueId();
        service.execute(new SaveTask(uuid, data, unlock));
    }

    public void save(List<Player> list, boolean unlock) {
        Map<UUID, String> map = new LinkedHashMap<>();
        for (Player p : list) {
            map.put(p.getUniqueId(), data(p));
        }
        service.execute(new SaveTask(map, unlock));
    }

    public void load(Player player) {
        if (player == null || !player.isOnline()) {
            throw new NullPointerException();
        }
        service.execute(new LoadTask(player.getUniqueId()));
    }

    public void load(Player player, UUID uuid, String value) {
        if (player != null && player.isOnline()) {
            JsonArray array = parser.parse(value).getAsJsonArray();
            load(player, array);
            compond.state(uuid, null);
            compond.map().remove(uuid);
        } else {
            /*
             * Player is null or offline here but the player's data on the
             * database has been locked. Perform an unlock task. This is an
             * infrequent case.
             */
            service.execute(new UnlockTask(uuid));
        }
    }

    private String data(Player player) {
        ItemStack[] inventory = player.getInventory().getContents();
        ItemStack[] armors = player.getInventory().getArmorContents();
        ItemStack[] chest = player.getEnderChest().getContents();
        Collection<PotionEffect> effects = player.getActivePotionEffects();
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        builder.append(player.getHealth()).append(',');
        builder.append(player.getFoodLevel()).append(',');
        builder.append(ExpsUtil.UTIL.getExp(player)).append(',');
        builder.append(getStackData(inventory)).append(',');
        builder.append(getStackData(armors)).append(',');
        builder.append(getStackData(chest)).append(',');
        builder.append(getEffectData(effects)).append(',');
        builder.append(player.getInventory().getHeldItemSlot());
        builder.append(']');
        return builder.toString();
    }

    private void load(Player p, JsonArray array) {
        if (Configs.SYN_HEAL) {
            double j = array.get(0).getAsDouble();
            double d = j <= p.getMaxHealth() ?
                    j != 0 ? j : p.getHealth() :
                    p.getMaxHealth();
            p.setHealth(d);
        }
        if (Configs.SYN_FOOD) {
            p.setFoodLevel(array.get(1).getAsInt());
        }
        if (Configs.SYN_EXPS) {
            ExpsUtil.UTIL.setExp(p, array.get(2).getAsInt());
        }
        if (Configs.SYN_INVT) {
            ItemStack[] stacks = arrayToStacks(array.get(3).getAsJsonArray());
            ItemStack[] armors = arrayToStacks(array.get(4).getAsJsonArray());
            int hold = array.size() > 7 ?
                    array.get(7).getAsInt() :
                    4;
            p.getInventory().setContents(stacks);
            p.getInventory().setArmorContents(armors);
            p.getInventory().setHeldItemSlot(hold);
        }
        if (Configs.SYN_CEST) {
            ItemStack[] stacks = arrayToStacks(array.get(5).getAsJsonArray());
            p.getEnderChest().setContents(stacks);
        }
        if (Configs.SYN_EFCT) {
            for (PotionEffect effect : p.getActivePotionEffects()) {
                p.removePotionEffect(effect.getType());
            }
            JsonArray input = array.get(6).getAsJsonArray();
            Collection<PotionEffect> effects = arrayToEffects(input);
            p.addPotionEffects(effects);
        }
    }

    private Collection<PotionEffect> arrayToEffects(JsonArray effects) {
        List<PotionEffect> out = new ArrayList<PotionEffect>();
        for (JsonElement element : effects) {
            JsonArray array = element.getAsJsonArray();
            String i = array.get(0).getAsString();
            int j = array.get(1).getAsInt();
            PotionEffect effect = new PotionEffect(PotionEffectType.
                    getByName(i), j, array.get(2).getAsInt(),
                    array.get(3).getAsBoolean());
            out.add(effect);
        }
        return out;
    }

    private ItemStack[] arrayToStacks(JsonArray array) {
        ArrayBuilder<ItemStack> builder = new ArrayBuilder<>();
        for (JsonElement element : array) {
            if (element.isJsonNull()) {
                builder.append(new ItemStack(Material.AIR));
            } else {
                String data = element.getAsString();
                builder.append(ItemUtil.UTIL.getItemStack(data));
            }
        }
        return builder.build(ItemStack.class);
    }

    private String getEffectData(Collection<PotionEffect> effects) {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        Iterator<PotionEffect> it = effects.iterator();
        for (; it.hasNext();) {
            PotionEffect effect = it.next();
            builder.append('[');
            builder.append('\"');
            builder.append(effect.getType().getName());
            builder.append('\"').append(',');
            builder.append(effect.getDuration()).append(',');
            builder.append(effect.getAmplifier()).append(',');
            builder.append(effect.isAmbient());
            builder.append(']');
            if (it.hasNext()) {
                builder.append(',');
            }
        }
        builder.append(']');
        return builder.toString();
    }

    private String getStackData(ItemStack[] stacks) {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for (int i = 0; i < stacks.length; i++) {
            if (i > 0) {
                builder.append(",");
            }
            ItemStack stack = stacks[i];
            if (stack != null && stack.getType() != Material.AIR) {
                CraftItemStack copy = CraftItemStack.asCraftCopy(stack);
                builder.append('\"');
                builder.append(ItemUtil.UTIL.getString(copy));
                builder.append('\"');
            } else {
                builder.append("null");
            }
        }
        builder.append(']');
        return builder.toString();
    }

    public enum State {
        CONN_DONE,
        JOIN_WAIT,
        JOIN_DONE,
        JOIN_FAID
    }

}
