package com.mengcraft.playersql.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.mengcraft.playersql.Main;
import com.mengcraft.playersql.SwitchRequest;
import com.mengcraft.playersql.SwitchRequest.Manager;

public class SendCommand implements CommandExecutor
{
    
    private final Manager switchManager = SwitchRequest.MANAGER;
    
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
            switchManager.send(arg0, arg3[1], arg3[2]);
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
