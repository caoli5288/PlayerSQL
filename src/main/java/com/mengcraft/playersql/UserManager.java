package com.mengcraft.playersql;

import com.avaje.ebean.Update;
import com.mengcraft.playersql.lib.ExpUtil;
import com.mengcraft.playersql.lib.ItemUtil;
import com.mengcraft.playersql.lib.JSONUtil;
import com.mengcraft.playersql.task.DailySaveTask;
import com.mengcraft.simpleorm.EbeanHandler;
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
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created on 16-1-2.
 */
public final class UserManager {

    public static final UserManager INSTANCE = new UserManager();
    public static final ItemStack AIR = new ItemStack(Material.AIR);

    private final Map<UUID, BukkitTask> taskMap;
    private final List<UUID> locked;

    private PluginMain main;
    private ItemUtil itemUtil;
    private ExpUtil expUtil;
    private EbeanHandler db;

    private UserManager() {
        this.taskMap = new ConcurrentHashMap<>();
        this.locked = new ArrayList<>();
    }

    public void addFetched(User user) {
        main.runTask(() -> pend(user));
    }

    /**
     * @return The user, or <code>null</code> if not exists.
     */
    public User fetchUser(UUID uuid) {
        return db.find(User.class, uuid);
    }

    public void saveUser(Player p, boolean lock) {
        saveUser(getUserData(p, lock), lock);
    }

    public void saveUser(User user, boolean lock) {
        user.setLocked(lock);
        db.update(user);
        if (Config.DEBUG) {
            main.info("Save user data " + user.getUuid() + " done!");
        }
    }

    public void lockUserData(UUID uuid) {
        Update<User> update = db.getServer().createUpdate(User.class, "update PLAYERSQL set locked = :locked where uuid = :uuid");
        update.set("locked", true);
        update.set("uuid", uuid.toString());
        int result = update.execute();
        if (Config.DEBUG) {
            if (result == 1) {
                main.info("Lock user data " + uuid + " done.");
            } else {
                main.info("Lock user data " + uuid + " faid!");
            }
        }
    }

    public User getUserData(UUID id, boolean b) {
        return getUserData(main.getServer().getPlayer(id), b);
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
                p.closeInventory();
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

    public void unlockUser(UUID uuid, boolean scheduled) {
        if (scheduled) {
            main.runTask(() -> unlockUser(uuid));
        } else {
            unlockUser(uuid);
        }
    }

    private void unlockUser(UUID uuid) {
        while (isLocked(uuid)) {
            locked.remove(uuid);
        }
    }

    private void pend(User user) {
        Player player = this.main.getPlayer(user.getUuid());
        if (player != null && player.isOnline()) {
            pend(user, player);
        } else if (Config.DEBUG) {
            this.main.info(new PluginException("User " + user.getUuid() + " not found!"));
        }
    }

    private void pend(User polled, Player player) {
        synchronized (polled) {
            if (Config.SYN_INVENTORY) {
                player.closeInventory();
                player.getInventory().setContents(toStack(polled.getInventory()));
                player.getInventory().setArmorContents(toStack(polled.getArmor()));
                player.getInventory().setHeldItemSlot(polled.getHand());
                player.updateInventory();
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
                this.main.info(e);
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
                this.main.info(e);
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
            this.main.info("No task can be canceled for " + uuid + '!');
        }
    }

    public void createTask(UUID uuid) {
        if (Config.DEBUG) {
            this.main.info("Scheduling daily save task for user " + uuid + '.');
        }
        DailySaveTask saveTask = new DailySaveTask();
        BukkitTask task = this.main.runTaskTimer(saveTask, 6000);
        saveTask.setUuid(uuid);
        saveTask.setUserManager(this);
        saveTask.setTaskId(task.getTaskId());
        BukkitTask old = this.taskMap.put(uuid, task);
        if (old != null) {
            if (Config.DEBUG) {
                this.main.info("Already scheduled task for user " + uuid + '!');
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
