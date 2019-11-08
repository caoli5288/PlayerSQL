package com.mengcraft.playersql;

import com.mengcraft.playersql.lib.CustomInventory;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

public class Commands implements CommandExecutor {

    private final BiRegistry<Player, Iterator<String>> registry = new BiRegistry<>();

    public Commands() {
        registry.register("open", this::open);
        registry.register("open inventory", this::openInventory);
        registry.register("open chest", this::openChest);
    }

    private void openChest(Player player, Iterator<String> iterator) {
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

    private void open(Player player, Iterator<String> iterator) {
        registry.handle("open " + iterator.next(), player, iterator);
    }

    private void openInventory(Player player, Iterator<String> iterator) {
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
        registry.handle(iterator.next(), (Player) sender, iterator);
        return true;
    }
}
