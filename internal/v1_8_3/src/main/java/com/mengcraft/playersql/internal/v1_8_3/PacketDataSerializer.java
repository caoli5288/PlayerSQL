package com.mengcraft.playersql.internal.v1_8_3;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mengcraft.playersql.internal.IPacketDataSerializer;
import io.netty.buffer.ByteBuf;
import lombok.SneakyThrows;
import net.minecraft.server.v1_8_R3.NBTReadLimiter;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.io.DataInput;
import java.io.DataOutput;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Compatible with old 1.8.8 data format.
 */
public class PacketDataSerializer implements IPacketDataSerializer {

    private static Field handle;
    private static Method save;
    private static Method load;

    static {
        try {
            handle = CraftItemStack.class.getDeclaredField("handle");
            handle.setAccessible(true);
            save = NBTTagCompound.class.getDeclaredMethod("write", DataOutput.class);
            save.setAccessible(true);
            load = NBTTagCompound.class.getDeclaredMethod("load", DataInput.class, int.class, NBTReadLimiter.class);
            load.setAccessible(true);
        } catch (Exception ignored) {
        }
    }

    private final net.minecraft.server.v1_8_R3.PacketDataSerializer buf;

    @SneakyThrows
    public PacketDataSerializer(ByteBuf bytebuf) {
        buf = new net.minecraft.server.v1_8_R3.PacketDataSerializer(bytebuf);
    }

    @SneakyThrows
    public void write(ItemStack input) {
        if (input == null || input.getType() == Material.AIR) {
            return;
        }
        CraftItemStack item = input instanceof CraftItemStack ? ((CraftItemStack) input) : CraftItemStack.asCraftCopy(input);
        net.minecraft.server.v1_8_R3.ItemStack nms = (net.minecraft.server.v1_8_R3.ItemStack) handle.get(item);
        NBTTagCompound compound = new NBTTagCompound();
        nms.save(compound);
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        save.invoke(compound, output);
        buf.writeBytes(output.toByteArray());
    }

    @Override
    @SneakyThrows
    public ItemStack readItemStack() {
        byte[] tmp = new byte[buf.readableBytes()];
        buf.readBytes(tmp);
        NBTTagCompound compound = new NBTTagCompound();
        load.invoke(compound, ByteStreams.newDataInput(tmp), 0, NBTReadLimiter.a);
        return CraftItemStack.asCraftMirror(net.minecraft.server.v1_8_R3.ItemStack.createStack(compound));
    }

    @Override
    public ByteBuf buf() {
        return buf;
    }
}
