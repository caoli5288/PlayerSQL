package com.mengcraft.playersql;

import com.comphenix.protocol.utility.StreamSerializer;
import com.google.common.collect.Maps;
import com.mengcraft.simpleorm.EbeanHandler;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

public enum LocalDataMgr {

    INSTANCE;

    private final Map<UUID, LocalData> pool = Maps.newConcurrentMap();

    EbeanHandler db;

    public static void pick(Player p) {
        LocalData d = load(p);
        List<String> list = (List<String>) JSONValue.parse(d.getInventory());

        if (list.isEmpty()) {
            return;
        }

        ItemStack[] all = list.stream().map(l -> conv(l)).toArray(ItemStack[]::new);
        val out = p.getInventory().addItem(all).values();

        d.setInventory(out.isEmpty() ? "[]" : JSONArray.toJSONString(out.stream().map(item -> conv(item)).collect(toList())));

        PluginMain.runAsync(() -> INSTANCE.db.save(d));

        PluginMain.getMessenger().send(p, "transfer_back_okay", ChatColor.GREEN + "Items have been added to the inventory");
        if (!out.isEmpty()) {
            PluginMain.getMessenger().send(p, "transfer_back_overflow", ChatColor.RED + "Some items are not received. Please clean up the inventory and try again.");
        }
    }

    public static void transfer(Player p, boolean newbie) {// this method run async
        if (!ready()) {
            return;
        }

        LocalData d = load(p);
        if (!(d.getInventory() == null)) {
            return;
        }

        if (newbie) {
            d.setInventory("[]");
        } else {
            List<String> list = StreamSupport.stream(p.getInventory().spliterator(), false)
                    .filter(item -> !(item == null) && !(item.getType() == Material.AIR))
                    .map(item -> conv(item))
                    .collect(toList());

            d.setInventory(JSONArray.toJSONString(list));
            if (!list.isEmpty()) {
                PluginMain.getMessenger().send(p, "transfer_okay", "transfer_okay");
                String notice = PluginMain.getPlugin().getConfig().getString("transfer.extra-notify-command", "");
                if (!notice.isEmpty()) {
                    Arrays.stream(notice.split("\n")).forEach(origin -> {
                        if (origin.isEmpty()) {
                            return;
                        }

                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), origin.replace("%player_name%", p.getName()));
                    });
                }
            }

            p.getInventory().clear();
        }

        INSTANCE.db.save(d);
    }

    public static void transfer(Player p) {
        transfer(p, false);
    }

    public static boolean ready() {
        return Config.TRANSFER_ORIGIN && !(INSTANCE.db == null);
    }

    public static LocalData load(Player p) {
        return INSTANCE.pool.computeIfAbsent(p.getUniqueId(), id -> {
            LocalData d = INSTANCE.db.find(LocalData.class, id);
            if (d == null) {
                d = INSTANCE.db.bean(LocalData.class);
                d.setId(p.getUniqueId());
            }
            return d;
        });
    }

    @SneakyThrows
    private static String conv(ItemStack item) {
        return StreamSerializer.getDefault().serializeItemStack(item);
    }

    public static void quit(Player p) {
        INSTANCE.pool.remove(p.getUniqueId());
    }

    @SneakyThrows
    private static ItemStack conv(String line) {
        return StreamSerializer.getDefault().deserializeItemStack(line);
    }

}
