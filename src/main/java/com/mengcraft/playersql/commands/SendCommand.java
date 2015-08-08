package com.mengcraft.playersql.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import com.mengcraft.playersql.Main;
import com.mengcraft.playersql.api.PlayerPreSwitchServerEvent;

public class SendCommand implements CommandExecutor
{
    
    private final Server server = Bukkit.getServer();
    private final PluginManager pm = server.getPluginManager();
    
    private final String[] info;
    
    public SendCommand()
    {
        this.info = new String[] {
                ChatColor.GOLD + "/playersql send <player> <target>"
        };
    }

    @Override
    public boolean onCommand(CommandSender arg0, Command arg1, String arg2,
            String[] arg3) {
        if (arg3.length != 3) {
            arg0.sendMessage(info);
        } else if (arg3[0].equals("send")) {
            @SuppressWarnings("deprecation")
            Player p = server.getPlayerExact(arg3[1]);
            if (p == null) {
                arg0.sendMessage(ChatColor.DARK_RED + "Player not found!");
            } else {
                pm.callEvent(new PlayerPreSwitchServerEvent(p, arg3[2]));
            }
        } else {
            arg0.sendMessage(info);
        }
        return false;
    }
    
    public void register(Main main)
    {
        main.getCommand("playersql").setExecutor(this);
    }
    
}
