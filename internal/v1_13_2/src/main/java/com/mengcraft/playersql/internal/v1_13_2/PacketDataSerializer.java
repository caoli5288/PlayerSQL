package com.mengcraft.playersql.internal.v1_13_2;

import com.mengcraft.playersql.internal.IPacketDataSerializer;
import io.netty.buffer.ByteBuf;
import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

public class PacketDataSerializer implements IPacketDataSerializer {

    private static Field handle;

    static {
        try {
            handle = CraftItemStack.class.getDeclaredField("handle");
            handle.setAccessible(true);
        } catch (NoSuchFieldException ignored) {
        }
    }

    private final net.minecraft.server.v1_13_R2.PacketDataSerializer buf;

    @SneakyThrows
    public PacketDataSerializer(ByteBuf bytebuf) {
        buf = new net.minecraft.server.v1_13_R2.PacketDataSerializer(bytebuf);
    }

    @SneakyThrows
    public void write(ItemStack input) {
        if (input == null || input.getType() == Material.AIR) {
            return;
        }
        CraftItemStack item = input instanceof CraftItemStack ? ((CraftItemStack) input) : CraftItemStack.asCraftCopy(input);
        net.minecraft.server.v1_13_R2.ItemStack nms = (net.minecraft.server.v1_13_R2.ItemStack) handle.get(item);
        buf.a(nms);
    }

    @Override
    public ItemStack readItemStack() {
        return CraftItemStack.asCraftMirror(buf.k());
    }

    @Override
    public ByteBuf buf() {
        return buf;
    }
}
