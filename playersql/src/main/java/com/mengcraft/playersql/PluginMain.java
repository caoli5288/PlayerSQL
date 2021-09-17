package com.mengcraft.playersql;

//import com.comphenix.protocol.ProtocolLibrary;

import com.google.common.io.ByteStreams;
import com.mengcraft.playersql.lib.MetricsLite;
import com.mengcraft.playersql.lib.SetExpFix;
import com.mengcraft.playersql.locker.EventLocker;
import com.mengcraft.playersql.peer.PlayerSqlProtocol;
import com.mengcraft.simpleorm.EbeanHandler;
import com.mengcraft.simpleorm.EbeanManager;
import com.mengcraft.simpleorm.ORM;
import com.mengcraft.simpleorm.lib.MavenLibs;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.logging.Level;

import static org.bukkit.Bukkit.getPluginManager;

/**
 * Created on 16-1-2.
 */
public class PluginMain extends JavaPlugin implements Executor {

    @Getter
    private static Messenger messenger;
    @Getter
    private static PluginMain plugin;
    private MetricsLite metric;
    private static boolean applyNullUserdata;

    @Override
    public void onLoad() {
        saveDefaultConfig();
        try {
            Class.forName("net.jpountz.lz4.LZ4Compressor");
        } catch (ClassNotFoundException e) {
            MavenLibs.of("org.lz4:lz4-java:1.8.0").load();
        }
        applyNullUserdata = getConfig().getBoolean("plugin.apply-null-userdata", false);
    }

    @SneakyThrows
    public void onEnable() {
        plugin = this;
        metric = new MetricsLite(this);

        if (!DataSerializer.valid()) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "!!! playersql item serializer not compatible. Please install ProtocolLib or feedback to me.");
            return;
        }

        getConfig().options().copyDefaults(true);
        saveConfig();

        messenger = new Messenger(this);

        Plugin dependency = Bukkit.getPluginManager().getPlugin("SimpleORM");
        if (dependency == null) {
            File dst = new File(getFile().getParentFile(), "simpleorm.jar");
            InputStream remote = new URL("jitpack.io/com/github/caoli5288/simpleorm/-SNAPSHOT/simpleorm--SNAPSHOT.jar").openStream();
            ByteStreams.copy(remote, new FileOutputStream(dst));
            dependency = Bukkit.getPluginManager().loadPlugin(dst);
        }
        if (!dependency.isEnabled()) {
            Bukkit.getPluginManager().enablePlugin(dependency);
        }

        ORM.loadLibrary(this);

        EbeanHandler db = EbeanManager.DEFAULT.getHandler(this);
        if (db.isNotInitialized()) {
            db.define(PlayerData.class);

            db.setMaxSize(getConfig().getInt("plugin.max-db-connection"));
            db.initialize();
        }

        db.install();

        if (Config.TRANSFER_ORIGIN) {
            EbeanHandler b = new EbeanHandler(this);
            b.setUrl("jdbc:sqlite:" + new File(getDataFolder(), "local_transfer.sqlite"));
            b.setMaxSize(1);
            b.setUserName("i7mc");
            b.setPassword("i7mc");

            b.define(LocalData.class);
            b.initialize();

            b.install(true);

            LocalDataMgr.INSTANCE.db = b;

            PluginHelper.addExecutor(this, "psqltrans", this::trans);
        }

        UserManager manager = UserManager.INSTANCE;
        manager.setMain(this);
        manager.setDb(db);

        EventExecutor executor = new EventExecutor(this);

        getServer().getPluginManager().registerEvents(executor, this);
        try {
            getServer().getPluginManager().registerEvents(new ExtendEventExecutor(manager), this);
        } catch (Exception ignore) {
        }// There is some event since 1.8.

//        if (getConfig().getBoolean("plugin.use-protocol-locker", false) && getPluginManager().isPluginEnabled("ProtocolLib")) {
//            ProtocolLibrary.getProtocolManager().addPacketListener(ProtocolBasedLocker.b(this));
//        } else {
        getPluginManager().registerEvents(new EventLocker(), this);
//        }

        getServer().getMessenger().registerOutgoingPluginChannel(this, PlayerSqlProtocol.NAMESPACE);
        getServer().getMessenger().registerIncomingPluginChannel(this, PlayerSqlProtocol.NAMESPACE, executor);

        getCommand("playersql").setExecutor(new Commands());

        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "PlayerSQL enabled! Donate me plz. https://www.paypal.me/2732000916/5");
    }

    public void trans(CommandSender sender, List<String> input) {
        LocalDataMgr.pick((Player) sender);
    }

    @Override
    public void onDisable() {
        for (Player p : getServer().getOnlinePlayers()) {
            UserManager.INSTANCE.saveUser(p, false);
        }
    }

    public Player getPlayer(UUID uuid) {
        return getServer().getPlayer(uuid);
    }

    public void log(Exception e) {
        if (Config.DEBUG) {
            getLogger().log(Level.SEVERE, e.toString(), e);
        }
    }

    public void log(String info) {
        getLogger().info(info);
    }

    public void debug(String line) {
        if (Config.DEBUG) {
            log(line);
        }
    }

    public static void resetPlayerState(Player player) {
        if (Config.SYN_INVENTORY) {
            player.getInventory().clear();
        }
        if (Config.SYN_HEALTH) {
            player.resetMaxHealth();
            player.setHealth(player.getMaxHealth());
        }
        if (Config.SYN_EXP) {
            SetExpFix.setTotalExperience(player, 0);
        }
        if (Config.SYN_FOOD) {
            player.setFoodLevel(20);
        }
        if (Config.SYN_EFFECT) {
            for (PotionEffect effect : player.getActivePotionEffects()) {
                player.removePotionEffect(effect.getType());
            }
        }
        if (Config.SYN_CHEST) {
            player.getEnderChest().clear();
        }
    }

    @Override
    public void execute(Runnable command) {
        Bukkit.getScheduler().runTask(this, command);
    }

    public static CompletableFuture<Void> runAsync(Runnable r) {
        return CompletableFuture.runAsync(r).exceptionally(thr -> {
            Bukkit.getLogger().log(Level.SEVERE, "" + thr, thr);
            return null;
        });
    }

    public void run(Runnable r) {
        getServer().getScheduler().runTask(this, r);
    }

    public static boolean isApplyNullUserdata() {
        return applyNullUserdata;
    }

    public static boolean nil(Object i) {
        return i == null;
    }

}
