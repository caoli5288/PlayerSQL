package com.mengcraft.playersql;

import com.mengcraft.playersql.lib.ExpUtil;
import com.mengcraft.playersql.lib.IOBlocking;
import com.mengcraft.playersql.lib.ItemUtil;
import com.mengcraft.playersql.lib.JSONUtil;
import com.mengcraft.playersql.task.DailySaveTask;
import com.mengcraft.simpleorm.EbeanHandler;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.mengcraft.playersql.PluginMain.nil;

/**
 * Created on 16-1-2.
 */
public enum UserManager {

    INSTANCE;

    public static final ItemStack AIR = new ItemStack(Material.AIR);

    private final Map<UUID, BukkitRunnable> scheduled = new HashMap<>();
    private final List<UUID> locked = new ArrayList<>();

    private PluginMain main;
    private ItemUtil itemUtil;
    private ExpUtil expUtil;
    private EbeanHandler db;

    public void addFetched(User user) {
        main.run(() -> pend(user));
    }

    /**
     * @return The user, or <code>null</code> if not exists.
     */
    public User fetchUser(UUID uuid) {
        return db.find(User.class, uuid);
    }

    public void saveUser(Player p, boolean lock) {
        saveUser(getUserData(p, !lock), lock);
    }

    public void saveUser(User user, boolean lock) {
        user.setLocked(lock);
        db.update(user);
        if (Config.DEBUG) {
            main.log("Save user data " + user.getUuid() + " done!");
        }
    }

    @IOBlocking
    public void updateDataLock(UUID who, boolean lock) {
        val update = db.getServer().createUpdate(User.class, "update " + User.TABLE_NAME +
                " set locked = :locked where uuid = :uuid");
        update.set("locked", lock);
        update.set("uuid", who.toString());
        int result = update.execute();
        if (Config.DEBUG) {
            if (result == 1) {
                main.log("Update " + who + " lock to " + lock + " okay");
            } else {
                main.log(new PluginException("Update " + who + " lock to " + lock + " failed"));
            }
        }
    }

    private void closeInventory(Player p) {
        // Try fix some duplicate item issue
        val view = p.getOpenInventory();
        if (!nil(view)) {
            val cursor = view.getCursor();
            if (!nil(cursor)) {
                view.setCursor(null);
                val d = p.getInventory().addItem(cursor);
                if (!d.isEmpty()) {
                    // Bypass to opened inventory
                    for (val item : d.values()) {
                        view.getTopInventory().addItem(item);
                    }
                }
            }
        }
    }

    public User getUserData(UUID id, boolean closeInventory) {
        val p = main.getServer().getPlayer(id);
        if (!nil(p)) {
            return getUserData(p, closeInventory);
        }
        return null;
    }

    public User getUserData(Player p, boolean closeInventory) {
        User user = new User();
        user.setUuid(p.getUniqueId());
        if (Config.SYN_HEALTH) {
            user.setHealth(p.getHealth());
        }
        if (Config.SYN_FOOD) {
            user.setFood(p.getFoodLevel());
        }
        if (Config.SYN_INVENTORY) {
            if (closeInventory) {
                closeInventory(p);
            }
            user.setInventory(toString(p.getInventory().getContents()));
            user.setArmor(toString(p.getInventory().getArmorContents()));
            user.setHand(p.getInventory().getHeldItemSlot());
        }
        if (Config.SYN_CHEST) {
            user.setChest(toString(p.getEnderChest().getContents()));
        }
        if (Config.SYN_EFFECT) {
            user.setEffect(toString(p.getActivePotionEffects()));
        }
        if (Config.SYN_EXP) {
            user.setExp(this.expUtil.getExp(p));
        }
        return user;
    }

    public boolean isLocked(UUID uuid) {
        return this.locked.indexOf(uuid) != -1;
    }

    public boolean isNotLocked(UUID uuid) {
        return locked.indexOf(uuid) == -1;
    }

    public void lockUser(UUID uuid) {
        this.locked.add(uuid);
    }

    public void unlockUser(UUID uuid) {
        while (isLocked(uuid)) {
            locked.remove(uuid);
        }
    }

    private void pend(User user) {
        val player = main.getPlayer(user.getUuid());
        if (!nil(player) && player.isOnline()) {
            try {
                pend(user, player);
            } catch (Exception e) {
                if (Config.KICK_LOAD_FAILED) {
                    player.kickPlayer(Config.KICK_LOAD_MESSAGE);
                }
                if (Config.DEBUG) {
                    main.log(e);
                } else {
                    main.log(e.toString());
                }
            }
        } else if (Config.DEBUG) {
            main.log(new PluginException("Player " + user.getUuid() + " not found"));
        }
    }

    private void pend(User polled, Player player) {
        if (Config.SYN_INVENTORY) {
            val fetched = toStack(polled.getInventory());
            player.closeInventory();
            val pack = player.getInventory();
            if (fetched.length > pack.getSize()) {// Fixed #36
                int size = pack.getSize();
                pack.setContents(Arrays.copyOf(fetched, size));
                val out = pack.addItem(Arrays.copyOfRange(fetched, size, fetched.length));
                if (!out.isEmpty()) {
                    val location = player.getLocation();
                    out.forEach((o, item) -> player.getWorld().dropItem(location, item));
                }
            } else {
                pack.setContents(fetched);
            }
            pack.setArmorContents(toStack(polled.getArmor()));
            pack.setHeldItemSlot(polled.getHand());
            player.updateInventory();// Force update needed
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
        createTask(player.getUniqueId());
        unlockUser(player.getUniqueId());
    }

    @SuppressWarnings("unchecked")
    private List<PotionEffect> toEffect(String input) {
        List<List> parsed = JSONUtil.parseArray(input);
        List<PotionEffect> output = new ArrayList<>(parsed.size());
        for (List<Number> entry : parsed) {
            output.add(new PotionEffect(PotionEffectType.getById(entry.get(0).intValue()), entry.get(1).intValue(), entry.get(2).intValue()));
        }
        return output;
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    private ItemStack[] toStack(String input) {
        List<String> list = JSONUtil.parseArray(input);
        List<ItemStack> output = new ArrayList<>(list.size());
        for (String line : list) {
            if (nil(line)) {
                output.add(AIR);
            } else {
                output.add(itemUtil.convert(line));
            }
        }
        return output.toArray(new ItemStack[list.size()]);
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
                this.main.log(e);
            }
        return array.toString();
    }

    @SuppressWarnings("unchecked")
    private String toString(Collection<PotionEffect> effects) {
        val out = new JSONArray();
        for (PotionEffect effect : effects) {
            val sub = new JSONArray();
            sub.add(effect.getType().getId());
            sub.add(effect.getDuration());
            sub.add(effect.getAmplifier());
            out.add(sub);
        }
        return out.toString();
    }

    public void cancelTask(int i) {
        this.main.getServer().getScheduler().cancelTask(i);
    }

    public void cancelTask(UUID uuid) {
        BukkitRunnable task = scheduled.remove(uuid);
        if (task != null) {
            task.cancel();
        } else if (Config.DEBUG) {
            this.main.log("No task can be canceled for " + uuid + '!');
        }
    }

    public void createTask(UUID who) {
        if (Config.DEBUG) {
            this.main.log("Scheduling daily save task for user " + who + '.');
        }
        DailySaveTask task = new DailySaveTask();
        task.setWho(who);
        task.runTaskTimer(main, 6000, 6000);
        BukkitRunnable old = scheduled.put(who, task);
        if (old != null) {
            old.cancel();
            if (Config.DEBUG) {
                this.main.log("Already scheduled task for user " + who + '!');
            }
        }
    }

    public void setItemUtil(ItemUtil itemUtil) {
        this.itemUtil = itemUtil;
    }

    public void setExpUtil(ExpUtil expUtil) {
        this.expUtil = expUtil;
    }

    public void setMain(PluginMain main) {
        this.main = main;
    }

    public PluginMain getMain() {
        return main;
    }

    public void setDb(EbeanHandler db) {
        this.db = db;
    }

    public void newUser(UUID uuid) {
        User user = new User();
        user.setUuid(uuid);
        user.setLocked(true);
        db.save(user);
    }
}
