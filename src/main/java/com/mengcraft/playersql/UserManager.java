package com.mengcraft.playersql;

import com.mengcraft.playersql.lib.ExpUtil;
import com.mengcraft.playersql.lib.ItemUtil;
import com.mengcraft.playersql.lib.JSONUtil;
import com.mengcraft.playersql.task.DailySaveTask;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONArray;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created on 16-1-2.
 */
public final class UserManager {

    public static final UserManager INSTANCE = new UserManager();
    public static final ItemStack AIR = new ItemStack(Material.AIR);

    private final Map<UUID, BukkitTask> taskMap;
    private final List<UUID> locked;
    private final Map<UUID, User> userMap;
    private final Queue<User> fetched;

    private PluginMain main;
    private ItemUtil itemUtil;
    private ExpUtil expUtil;

    private UserManager() {
        this.taskMap = new ConcurrentHashMap<>();
        this.locked = new ArrayList<>();
        this.userMap = new ConcurrentHashMap<>();
        this.fetched = new ConcurrentLinkedQueue<>();
    }

    /**
     * @return The user, or <code>null</code> if not exists.
     */
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
        User user = this.main.getDatabase().createEntityBean(User.class);
        synchronized (user) {
            user.setUuid(uuid);
            user.setLocked(true);
        }
        cacheUser(uuid, user);
    }

    public void cacheUser(UUID uuid, User user) {
        if (user == null) {
            userMap.remove(uuid);
        } else {
            userMap.put(uuid, user);
        }
    }

    public void saveUser(UUID uuid, boolean lock) {
        User user = this.userMap.get(uuid);
        if (user == null) {
            if (Config.DEBUG) {
                this.main.logException(new PluginException("User " + uuid + " not found!"));
            }
        } else {
            saveUser(user, lock);
        }
    }

    public void saveUser(User user, boolean lock) {
        synchronized (user) {
            if (lock) {
                user.setLocked(true);
            } else if (user.isLocked()) {
                user.setLocked(false);
            }
            this.main.getDatabase().save(user);
        }
        if (Config.DEBUG) {
            this.main.logMessage("Save user data " + user.getUuid() + " done!");
        }
    }

    public void syncUser(User user) {
        syncUser(user, false);
    }

    public void syncUser(User user, boolean closedInventory) {
        Player player = main.getPlayer(user.getUuid());
        synchronized (user) {
            if (Config.SYN_HEALTH) {
                user.setHealth(player.getHealth());
            }
            if (Config.SYN_FOOD) {
                user.setFood(player.getFoodLevel());
            }
            if (Config.SYN_INVENTORY) {
                if (closedInventory) {
                    player.closeInventory();
                }
                user.setInventory(toString(player.getInventory().getContents()));
                user.setArmor(toString(player.getInventory().getArmorContents()));
                user.setHand(player.getInventory().getHeldItemSlot());
            }
            if (Config.SYN_CHEST) {
                user.setChest(toString(player.getEnderChest().getContents()));
            }
            if (Config.SYN_EFFECT) {
                user.setEffect(toString(player.getActivePotionEffects()));
            }
            if (Config.SYN_EXP) {
                user.setExp(this.expUtil.getExp(player));
            }
        }
    }

    public void syncUser(UUID uuid, boolean closedInventory) {
        syncUser(userMap.get(uuid), closedInventory);
    }

    public boolean isUserLocked(UUID uuid) {
        return this.locked.indexOf(uuid) != -1;
    }

    public boolean isUserNotLocked(UUID uuid) {
        return this.locked.indexOf(uuid) == -1;
    }

    public void lockUser(UUID uuid) {
        this.locked.add(uuid);
    }

    public void unlockUser(UUID uuid, boolean scheduled) {
        if (Config.DEBUG) {
            main.logMessage("Unlock user task on " + Thread.currentThread().getName() + '.');
        }
        if (scheduled) {
            this.main.runTask(() -> unlockUser(uuid, false));
        } else {
            if (Config.DEBUG) {
                this.main.logMessage("Unlock user " + uuid + '!');
            }
            this.locked.remove(uuid);
        }
    }

    /**
     * Process fetched users.
     */
    public void pendFetched() {
        while (!this.fetched.isEmpty()) {
            try {
                pend(this.fetched.poll());
            } catch (Exception e) {
                main.logException(e);
            }
        }
    }

    private void pend(User user) {
        Player player = this.main.getPlayer(user.getUuid());
        if (player != null && player.isOnline()) {
            pend(user, player);
        } else this.main.runTaskAsynchronously(() -> {
            if (Config.DEBUG) {
                this.main.logException(new PluginException("User " + user.getUuid() + " not found!"));
            }
            saveUser(user, true);
        });
    }

    private void pend(User polled, Player player) {
        synchronized (polled) {
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
        }
        createTask(player.getUniqueId());
        unlockUser(player.getUniqueId(), false);
    }

    @SuppressWarnings("unchecked")
    private List<PotionEffect> toEffect(String data) {
        List<List> parsed = JSONUtil.parseArray(data, JSONUtil.EMPTY_ARRAY);
        List<PotionEffect> output = new ArrayList<>(parsed.size());
        for (List<Number> entry : parsed) {
            output.add(new PotionEffect(PotionEffectType.getById(entry.get(0).intValue()), entry.get(1).intValue(), entry.get(2).intValue()));
        }
        return output;
    }

    @SuppressWarnings("unchecked")
    private ItemStack[] toStack(String data) {
        List<String> parsed = JSONUtil.parseArray(data, JSONUtil.EMPTY_ARRAY);
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
    private String toString(Collection<PotionEffect> effects) {
        JSONArray array = new JSONArray();
        for (PotionEffect effect : effects)
            array.add(new JSONArray() {{
                add(effect.getType().getId());
                add(effect.getDuration());
                add(effect.getAmplifier());
            }});
        return array.toString();
    }

    public void cancelTask(int i) {
        this.main.getServer().getScheduler().cancelTask(i);
    }

    public void cancelTask(UUID uuid) {
        BukkitTask task = taskMap.remove(uuid);
        if (task != null) {
            task.cancel();
        } else if (Config.DEBUG) {
            this.main.logMessage("No task can be canceled for " + uuid + '!');
        }
    }

    public void createTask(UUID uuid) {
        if (Config.DEBUG) {
            this.main.logMessage("Scheduling daily save task for user " + uuid + '.');
        }
        DailySaveTask saveTask = new DailySaveTask();
        BukkitTask task = this.main.runTaskTimer(saveTask, 6000);
        synchronized (saveTask) {
            saveTask.setUuid(uuid);
            saveTask.setUserManager(this);
            saveTask.setTaskId(task.getTaskId());
        }
        BukkitTask old = this.taskMap.put(uuid, task);
        if (old != null) {
            if (Config.DEBUG) {
                this.main.logMessage("Already scheduled task for user " + uuid + '!');
            }
            old.cancel();
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

}
