package com.mengcraft.playersql;

import com.mengcraft.playersql.event.PlayerDataFetchedEvent;
import com.mengcraft.playersql.event.PlayerDataProcessedEvent;
import com.mengcraft.playersql.event.PlayerDataStoreEvent;
import com.mengcraft.playersql.internal.GuidResolveService;
import com.mengcraft.playersql.lib.SetExpFix;
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
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.mengcraft.playersql.PluginMain.nil;

/**
 * Created on 16-1-2.
 */
public enum UserManager {

    INSTANCE;

    public static final ItemStack AIR = new ItemStack(Material.AIR);

    private final Map<UUID, BukkitRunnable> scheduled = new HashMap<>();
    private final Set<UUID> locked = new HashSet<>();

    private PluginMain main;
    private EbeanHandler db;

    public void addFetched(Player player, PlayerData user) {
        main.run(() -> pend(player, user));
    }

    /**
     * @return The user, or <code>null</code> if not exists.
     */
    public PlayerData fetchUser(UUID uuid) {
        return db.find(PlayerData.class, uuid);
    }

    public List<PlayerData> fetchName(String name) {
        return db.getServer().find(PlayerData.class).where("name = :name").setParameter("name", name).findList();
    }

    public void saveUser(Player p, boolean lock) {
        saveUser(getUserData(p, !lock), lock);
    }

    public void saveUser(PlayerData user, boolean lock) {
        user.setLocked(lock);
        db.update(user);
        if (Config.DEBUG) {
            main.log("Save user data " + user.getUuid() + " done!");
        }
    }

    public void updateDataLock(UUID who, boolean lock) {
        val update = db.getServer().createUpdate(PlayerData.class, "update " + PlayerData.TABLE_NAME +
                " set locked = :locked where uuid = :uuid");
        update.set("locked", lock);
        update.set("uuid", who.toString());
        int result = update.execute();
        if (Config.DEBUG) {
            if (result == 1) {
                main.log("Update " + who + " lock to " + lock + " okay");
            } else {
                main.log(new IllegalStateException("Update " + who + " lock to " + lock + " failed"));
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

    public PlayerData getUserData(UUID id, boolean closeInventory) {
        val p = main.getServer().getPlayer(id);
        if (!nil(p)) {
            return getUserData(p, closeInventory);
        }
        return null;
    }

    public PlayerData getUserData(Player p, boolean closeInventory) {
        PlayerData user = new PlayerData();
        user.setUuid(GuidResolveService.getService().getGuid(p));
        user.setName(p.getName());
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
            user.setExp(SetExpFix.getTotalExperience(p));
        }
        PlayerDataStoreEvent.call(p, user);
        return user;
    }

    public static boolean isLocked(UUID uuid) {
        return INSTANCE.locked.contains(uuid);
    }

    public boolean isNotLocked(UUID uuid) {
        return !isLocked(uuid);
    }

    public void lockUser(Player player) {
        main.debug(String.format("manager.lockUser(%s)", player.getName()));
        locked.add(player.getUniqueId());
        if (main.getConfig().getBoolean("plugin.disable-filter-network")) {
            return;
        }
        Utils.setAutoRead(player, false);
    }

    public void unlockUser(Player player) {
        main.debug(String.format("manager.unlockUser(%s)", player.getName()));
        UUID uuid = player.getUniqueId();
        while (isLocked(uuid)) {
            locked.remove(uuid);
        }
        if (main.getConfig().getBoolean("plugin.disable-filter-network")) {
            return;
        }
        Utils.setAutoRead(player, true);
    }

    void onLoadFailed(Player who) {
        if (Config.KICK_LOAD_FAILED) {
            who.kickPlayer(PluginMain.getMessenger().find("kick_load", "Your game data loading error, please contact the operator"));
        } else {
            unlockUser(who);
            createTask(who);
        }
    }

    public void pend(Player who, PlayerData data) {
        if (who == null || !who.isOnline()) {
            main.log(new IllegalStateException("Player " + data.getUuid() + " not found"));
            return;
        }

        if (isNotLocked(who.getUniqueId())) {
            main.debug(String.format("manager.pend cancel pend ops, player %s not locked", who.getName()));
            return;
        }

        val event = PlayerDataFetchedEvent.call(who, data);
        if (event.isCancelled()) {
            onLoadFailed(who);
        } else {
            Exception exception = null;
            try {
                syncUserdata(who, data);
            } catch (Exception e) {
                exception = e;
                onLoadFailed(who);
                if (Config.DEBUG) {
                    main.log(e);
                } else {
                    main.log(e.toString());
                }
            }

            PlayerDataProcessedEvent.call(who, exception);
        }
    }

    private void syncUserdata(Player who, PlayerData data) {
        if (Config.SYN_INVENTORY) {
            val ctx = toStack(data.getInventory());
            who.closeInventory();
            val inv = who.getInventory();
            if (ctx.length > inv.getSize()) {// Fixed #36
                int size = inv.getSize();
                inv.setContents(Arrays.copyOf(ctx, size));
                val out = inv.addItem(Arrays.copyOfRange(ctx, size, ctx.length));
                if (!out.isEmpty()) {
                    val location = who.getLocation();
                    out.forEach((o, item) -> who.getWorld().dropItem(location, item));
                }
            } else {
                inv.setContents(ctx);
            }
            inv.setArmorContents(toStack(data.getArmor()));
            inv.setHeldItemSlot(data.getHand());
            who.updateInventory();// Force update needed
        }
        if (Config.SYN_HEALTH && who.getMaxHealth() >= data.getHealth()) {
            who.setHealth(data.getHealth() <= 0 && Config.OMIT_PLAYER_DEATH ? who.getMaxHealth() : data.getHealth());
        }
        if (Config.SYN_EXP) {
            SetExpFix.setTotalExperience(who, data.getExp());
        }
        if (Config.SYN_FOOD) {
            who.setFoodLevel(data.getFood());
        }
        if (Config.SYN_EFFECT) {
            for (val eff : who.getActivePotionEffects()) {
                who.removePotionEffect(eff.getType());
            }
            for (val eff : toEffect(data.getEffect())) {
                who.addPotionEffect(eff, true);
            }
        }
        if (Config.SYN_CHEST) {
            who.getEnderChest().setContents(toStack(data.getChest()));
        }
        createTask(who);
        unlockUser(who);
    }

    @SuppressWarnings("unchecked")
    private List<PotionEffect> toEffect(String input) {
        List<List> parsed = parseArray(input);
        List<PotionEffect> output = new ArrayList<>(parsed.size());
        for (List<Number> entry : parsed) {
            output.add(new PotionEffect(PotionEffectType.getById(entry.get(0).intValue()), entry.get(1).intValue(), entry.get(2).intValue()));
        }
        return output;
    }

    public static JSONArray parseArray(String in) {
        if (!nil(in)) {
            Object parsed = JSONValue.parse(in);
            if (parsed instanceof JSONArray) {
                return ((JSONArray) parsed);
            }
        }
        return new JSONArray();
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public ItemStack[] toStack(String input) {
        List<String> list = parseArray(input);
        List<ItemStack> output = new ArrayList<>(list.size());
        for (String line : list) {
            if (nil(line) || line.isEmpty()) {
                output.add(AIR);
            } else {
                output.add(DataSerializer.deserialize(line));
            }
        }
        return output.toArray(new ItemStack[list.size()]);
    }

    @SuppressWarnings("unchecked")
    public String toString(ItemStack[] stacks) {
        JSONArray array = new JSONArray();
        for (ItemStack stack : stacks)
            if (stack == null || stack.getAmount() == 0) {
                array.add("");
            } else try {
                array.add(DataSerializer.serialize(stack));
            } catch (Exception e) {
                main.log(e);
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

    public void cancelTimerSaver(UUID uuid) {
        val i = scheduled.remove(uuid);
        if (!nil(i)) {
            i.cancel();
        } else if (Config.DEBUG) {
            main.log("No task can be canceled for " + uuid + '!');
        }
    }

    public void createTask(Player player) {
        if (Config.DEBUG) {
            this.main.log("Scheduling daily save task for user " + player.getName() + '.');
        }
        val task = new DailySaveTask(player);
        task.runTaskTimer(main, 6000, 6000);
        val old = scheduled.put(player.getUniqueId(), task);
        if (!nil(old)) {
            old.cancel();
            if (Config.DEBUG) {
                this.main.log("Already scheduled task for user " + player.getName() + '!');
            }
        }
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
        PlayerData user = new PlayerData();
        user.setUuid(uuid);
        user.setLocked(true);
        db.save(user);
    }
}
