package com.mengcraft.playersql.internal;

import org.bukkit.entity.Player;

import java.util.UUID;

public class GuidResolveService {

    private static GuidResolveService service = new GuidResolveService();

    public UUID getGuid(Player player) {
        return player.getUniqueId();
    }

    public static GuidResolveService getService() {
        return service;
    }

    public static void setService(GuidResolveService service) {
        GuidResolveService.service = service;
    }
}
