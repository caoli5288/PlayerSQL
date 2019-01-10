package com.mengcraft.playersql.internal;

import io.netty.buffer.ByteBuf;
import org.bukkit.inventory.ItemStack;

import java.io.Closeable;

public interface IPacketDataSerializer extends Closeable {

    void write(ItemStack input);

    ItemStack readItemStack();

    default void close() {
        buf().release();
    }

    ByteBuf buf();

    default byte[] readAll() {
        ByteBuf buf = buf();
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        return data;
    }

    interface IFactory {

        IPacketDataSerializer create(ByteBuf buf);
    }
}
