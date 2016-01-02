package com.mengcraft.playersql;

import com.mengcraft.playersql.lib.ExpUtil;
import com.mengcraft.playersql.lib.ItemUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created on 16-1-2.
 */
public final class UserManager {

    private final Map<UUID, User> userMap = new ConcurrentHashMap<>();
    private final Queue<User> fetched = new ConcurrentLinkedQueue<>();

    private PluginMain main;
    private ItemUtil itemUtil;
    private ExpUtil expUtil;

    public User getUser(UUID uuid) {
        return this.userMap.get(uuid);
    }

    public void addFetched(User user) {
        this.fetched.offer(user);
    }

    /**
     * @return The user, or <code>null</code> if not exists.
     */
    public User fetchUser(UUID uuid) {
        return this.main.getDatabase().find(User.class, uuid);
    }

    /**
     * Create and cache a new user.
     */
    public void cacheUser(UUID uuid) {
        User user = this.main.getDatabase().createEntityBean(User.class)
                .setUuid(uuid)
                .setLocked(true);
        cacheUser(uuid, user);
    }

    public void cacheUser(UUID uuid, User user) {
        this.userMap.put(uuid, user);
    }

    public void saveUser(UUID uuid) {
        User user = getUser(uuid);
        if (user != null) {
            saveUser(user);
        } else if (Config.DEBUG) {
            this.main.logException(new PluginException("User " + uuid + " not cached!"));
        }
    }

    public void saveUser(User user) {
        this.main.getDatabase().save(user);
    }

    /**
     * Process fetched users.
     */
    public void pendFetched() {
        while (!this.fetched.isEmpty()) {
            pend(this.fetched.poll());
        }
    }

    private void pend(User polled) {
        Player player = this.main.getPlayer(polled.getUuid());
        if (player != null && player.isOnline()) {
            if (Config.SYN_INVENTORY) {
                player.closeInventory();
                player.getInventory().setContents(toStack(polled.getInventory()));
                player.getInventory().setArmorContents(toStack(polled.getArmor()));
                player.getInventory().setHeldItemSlot(polled.getHand());
            }
            if (Config.SYN_HEALTH && player.getMaxHealth() >= polled.getHealth()) {
                player.setHealth(polled.getHealth());
            }
            if (Config.SYN_EXP) {
                this.expUtil.setExp(player, polled.getExp());
            }
            if (Config.SYN_FOOD) {
                player.setFoodLevel(polled.getFood());
            }
            if (Config.SYN_EFFECT) {
                for (PotionEffect effect : toEffect(polled.getEffect())) {
                    player.addPotionEffect(effect, true);
                }
            }
            if (Config.SYN_CHEST) {
                player.getEnderChest().setContents(toStack(polled.getChest()));
            }
        } else {
            throw new RuntimeException();
        }
    }

    @SuppressWarnings("unchecked")
    private List<PotionEffect> toEffect(String data) {
        List<List> parsed = (List) JSONValue.parse(data);
        List<PotionEffect> output = new ArrayList<>(parsed.size());
        for (List<Number> entry : parsed) {
            output.add(new PotionEffect(PotionEffectType.getById(entry.get(0).intValue()), entry.get(1).intValue(), entry.get(2).intValue()));
        }
        return output;
    }

    @SuppressWarnings("unchecked")
    private ItemStack[] toStack(String data) {
        List<String> parsed = (List) JSONValue.parse(data);
        List<ItemStack> output = new ArrayList<>(parsed.size());
        for (String s : parsed)
            if (s == null) {
                output.add(AIR);
            } else try {
                output.add(this.itemUtil.convert(s));
            } catch (Exception e) {
                this.main.logException(e);
            }
        return output.toArray(new ItemStack[parsed.size()]);
    }

    @SuppressWarnings("unchecked")
    private String toString(ItemStack[] stacks) {
        JSONArray array = new JSONArray();
        for (ItemStack stack : stacks)
            if (stack == null || stack.getTypeId() == 0) {
                array.add(null);
            } else try {
                array.add(this.itemUtil.convert(stack));
            } catch (Exception e) {
                this.main.logException(e);
            }
        return array.toString();
    }

    @SuppressWarnings("unchecked")
    private String toString(List<PotionEffect> effects) {
        JSONArray array = new JSONArray();
        for (PotionEffect effect : effects)
            array.add(new JSONArray() {{
                add(effect.getType().getId());
                add(effect.getDuration());
                add(effect.getAmplifier());
            }});
        return array.toString();
    }

    public ItemUtil getItemUtil() {
        return itemUtil;
    }

    public void setItemUtil(ItemUtil itemUtil) {
        this.itemUtil = itemUtil;
    }

    public ExpUtil getExpUtil() {
        return expUtil;
    }

    public void setExpUtil(ExpUtil expUtil) {
        this.expUtil = expUtil;
    }

    public PluginMain getMain() {
        return main;
    }

    public void setMain(PluginMain main) {
        this.main = main;
    }

    private static final ItemStack AIR = new ItemStack(Material.AIR);

}
