package com.mengcraft.playersql;

import lombok.val;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

/**
 * Created on 16-4-13.
 */
public class Messenger {

    private final static String PREFIX = "message.";
    private final Plugin plugin;

    public Messenger(Plugin plugin) {
        this.plugin = plugin;
    }

    private String path(String path) {
        return PREFIX + path;
    }

    private String multi(Object found) {
        val input = (List) found;
        val b = new StringBuilder();
        int size = input.size();
        for (int l = 0; l < size; l++) {
            if (l > 0) {
                b.append('\n');
            }
            b.append(input.get(l));
        }
        return b.toString();
    }

    public String find(String path, String input) {
        val found = plugin.getConfig().get(path(path), null);
        if (found == null) {
            if (input == null || input.isEmpty()) return path;
            if (input.indexOf('\n') == -1) {
                plugin.getConfig().set(path(path), input);
                plugin.saveConfig();
            } else {
                plugin.getConfig().set(path(path), Arrays.asList(input.split("\n")));
                plugin.saveConfig();
            }
            return input;
        } else if (found instanceof List) {
            return multi(found);
        }
        return found.toString();
    }

    public String find(String path) {
        return find(path, "");
    }

    public void sendLine(CommandSender receive, String line) {
        receive.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
    }

    public void sendMessage(CommandSender receive, String message) {
        if (message.indexOf('\n') == -1) {
            sendLine(receive, message);
        } else {
            for (String line : message.split("\n")) {
                sendLine(receive, line);
            }
        }
    }

    public void send(CommandSender receive, String path, String input) {
        sendMessage(receive, find(path, input));
    }

    public void send(CommandSender receive, String path) {
        send(receive, path, "");
    }

}
