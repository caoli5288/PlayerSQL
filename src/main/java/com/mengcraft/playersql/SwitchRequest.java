package com.mengcraft.playersql;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import com.mengcraft.playersql.api.PlayerPreSwitchServerEvent;

public class SwitchRequest {

    public static final Manager MANAGER = new Manager();

    private UUID player;
    private String target;

    public UUID getPlayer() {
        return player;
    }

    public void setPlayer(UUID player) {
        this.player = player;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public static class Manager {
        
        private final Server server = Bukkit.getServer();
        private final PluginManager pm = server.getPluginManager();
        
        private final Queue<SwitchRequest> queue;

        private Manager() {
            this.queue = new LinkedBlockingQueue<>();
        }

        public SwitchRequest poll() {
            return queue.poll();
        }

        public void offer(SwitchRequest request) {
            queue.offer(request);
        }

        public Queue<SwitchRequest> getQueue() {
            return queue;
        }
        
        public void send(CommandSender caller, String who, String target) {
            @SuppressWarnings("deprecation")
            Player p = server.getPlayerExact(who);
            if (p == null) {
                caller.sendMessage(ChatColor.DARK_RED + "Player not found!");
            } else {
                pm.callEvent(new PlayerPreSwitchServerEvent(p, target));
            }
        }

    }

}
