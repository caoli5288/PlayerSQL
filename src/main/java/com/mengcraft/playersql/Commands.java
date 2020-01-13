package com.mengcraft.playersql;

import com.mengcraft.playersql.lib.CustomInventory;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

public class Commands implements CommandExecutor {

    private final BiRegistry<CommandSender, Iterator<String>> registry = new BiRegistry<>();

    public Commands() {
        registry.register("open", this::open);
        registry.register("open inventory", this::openInventory);
        registry.register("open chest", this::openChest);
        registry.register("config", this::config);
    }

    private void config(CommandSender sender, Iterator<String> iterator) {
        FileConfiguration config = PluginMain.getPlugin().getConfig();
        String node = iterator.next();
        if (iterator.hasNext()) {
            String value = iterator.next();
            if (value.equalsIgnoreCase("true")) {
                config.set(node, true);
            } else if (value.equalsIgnoreCase("false")) {
                config.set(node, false);
            } else if (value.matches("\\d+")) {
                config.set(node, Integer.parseInt(value));
            } else {
                sender.sendMessage("unsupported operation");
            }
        } else {
            sender.sendMessage(String.format("%s = %s", node, config.get(node)));
        }
    }

    private void openChest(CommandSender sender, Iterator<String> iterator) {
        Player player = (Player) sender;
        String name = iterator.next();
        Player found = Bukkit.getPlayerExact(name);
        if (found != null) {
            player.openInventory(found.getInventory());
            return;
        }
        CompletableFuture.runAsync(() -> {
            PlayerData data = UserManager.INSTANCE.fetchName(name);
            if (data == null) {
                player.sendMessage("player not found");
                return;
            }
            if (data.isLocked()) {
                player.sendMessage("player current online");
                return;
            }
            ItemStack[] stacks = UserManager.INSTANCE.toStack(data.getChest());
            CustomInventory inventory = CustomInventory.onCreate(res -> {
                Inventory inv = Bukkit.createInventory(res, InventoryType.ENDER_CHEST);
                inv.setContents(stacks);
                return inv;
            }).onClose(res -> {
                data.setChest(UserManager.INSTANCE.toString(res.getContents()));
                CompletableFuture.runAsync(() -> UserManager.INSTANCE.saveUser(data, data.isLocked()));
            });
            CompletableFuture.runAsync(() -> player.openInventory(inventory.getInventory()), PluginMain.getPlugin());
        });
    }

    private void open(CommandSender player, Iterator<String> iterator) {
        registry.handle("open " + iterator.next(), player, iterator);
    }

    private void openInventory(CommandSender sender, Iterator<String> iterator) {
        Player player = (Player) sender;
        String name = iterator.next();
        Player found = Bukkit.getPlayerExact(name);
        if (found != null) {
            player.openInventory(found.getInventory());
            return;
        }
        CompletableFuture.runAsync(() -> {
            PlayerData data = UserManager.INSTANCE.fetchName(name);
            if (data == null) {
                player.sendMessage("player not found");
                return;
            }
            if (data.isLocked()) {
                player.sendMessage("player current online");
                return;
            }
            ItemStack[] stacks = UserManager.INSTANCE.toStack(data.getInventory());
            CustomInventory inventory = CustomInventory.onCreate(res -> {
                Inventory inv = Bukkit.createInventory(res, InventoryType.PLAYER);
                inv.setContents(stacks);
                return inv;
            }).onClose(res -> {
                data.setInventory(UserManager.INSTANCE.toString(res.getContents()));
                CompletableFuture.runAsync(() -> UserManager.INSTANCE.saveUser(data, data.isLocked()));
            });
            CompletableFuture.runAsync(() -> player.openInventory(inventory.getInventory()), PluginMain.getPlugin());
        });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            for (Object key : registry.getKeys()) {
                sender.sendMessage("/playersql " + key);
            }
            return true;
        }
        Iterator<String> iterator = Arrays.asList(args).iterator();
        registry.handle(iterator.next(), sender, iterator);
        return true;
    }
}
