package com.mengcraft.playersql;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
@Setter
public class UserState {

    private BukkitRunnable fetchTask;
    private PlayerData playerData;
    private boolean kicking;
}
