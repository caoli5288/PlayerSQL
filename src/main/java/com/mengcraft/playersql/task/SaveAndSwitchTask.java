package com.mengcraft.playersql.task;

import java.util.UUID;

import org.bukkit.entity.Player;

import com.mengcraft.playersql.SwitchRequest;

public class SaveAndSwitchTask extends SaveTask {

    private final String target;
    private final UUID player;

    public SaveAndSwitchTask(Player player, String data, String target) {
        super(player.getUniqueId(), data, true);
        this.target = target;
        this.player = player.getUniqueId();
    }

    @Override
    public void run() {
        super.run();

        SwitchRequest request = new SwitchRequest();
        request.setPlayer(player);
        request.setTarget(target);

        SwitchRequest.MANAGER.offer(request);
    }

}
