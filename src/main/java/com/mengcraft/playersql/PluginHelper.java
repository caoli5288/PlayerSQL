package com.mengcraft.playersql;

import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * Created on 17-1-18.
 */
public class PluginHelper {

    public static class Runner extends BukkitRunnable implements ICancellable {

        private final IRunner r;

        Runner(IRunner r) {
            this.r = r;
        }

        @Override
        public void run() {
            r.run(this);
        }
    }

    @SneakyThrows
    public static void addExecutor(Plugin plugin, Command command) {
        Field f = SimplePluginManager.class.getDeclaredField("commandMap");
        f.setAccessible(true);
        CommandMap map = (CommandMap) f.get(plugin.getServer().getPluginManager());
        Constructor<PluginCommand> init = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
        init.setAccessible(true);
        PluginCommand inject = init.newInstance(command.getName(), plugin);
        inject.setExecutor((who, __, label, input) -> command.execute(who, label, input));
        inject.setAliases(command.getAliases());
        inject.setDescription(command.getDescription());
        inject.setLabel(command.getLabel());
        inject.setName(command.getName());
        inject.setPermission(command.getPermission());
        inject.setPermissionMessage(command.getPermissionMessage());
        inject.setUsage(command.getUsage());
        map.register(plugin.getName().toLowerCase(), inject);
    }

    public interface IExec {

        void exec(CommandSender sender, List<String> list);
    }

    public interface ICancellable {

        void cancel();
    }

    public interface IRunner {

        void run(ICancellable r);
    }

    public static int run(Plugin plugin, int i, int repeat, IRunner r) {
        Runner out = new Runner(r);
        if (i > 0) {
            if (repeat > 0) {
                out.runTaskTimer(plugin, i, repeat);
            } else {
                out.runTaskLater(plugin, i);
            }
        } else {
            if (repeat > 0) {
                out.runTaskTimer(plugin, 0, repeat);
            } else {
                out.runTask(plugin);
            }
        }
        return out.getTaskId();
    }

    public static class Exec extends Command {

        private final IExec exec;

        Exec(String name, IExec exec) {
            super(name);
            this.exec = exec;
        }

        @Override
        public boolean execute(CommandSender who, String l, String[] i) {
            try {
                exec.exec(who, Arrays.asList(i));
                return true;
            } catch (Exception e) {
                who.sendMessage(ChatColor.RED + e.toString());
                Bukkit.getLogger().log(Level.WARNING, e.toString(), e);
            }
            return false;
        }
    }

    public static void addExecutor(Plugin plugin, String command, IExec exec) {
        addExecutor(plugin, command, null, exec);
    }

    public static void addExecutor(Plugin plugin, String command, String permission, IExec exec) {
        Exec e = new Exec(command, exec);
        e.setPermission(permission);
        e.setPermissionMessage(ChatColor.RED + "您没有权限执行此类指令，请联系管理！");
        addExecutor(plugin, e);
    }

}
