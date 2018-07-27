package com.mengcraft.playersql.internal;

import io.netty.buffer.ByteBuf;
import org.bukkit.inventory.ItemStack;

public interface IPacketDataSerializer {

    void write(ItemStack input);

    ItemStack readItemStack();

    ByteBuf buf();

    interface IFactory {

        IPacketDataSerializer create(ByteBuf buf);
    }
}
