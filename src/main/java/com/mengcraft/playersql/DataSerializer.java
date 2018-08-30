package com.mengcraft.playersql;

import com.comphenix.protocol.utility.StreamSerializer;
import com.mengcraft.playersql.internal.IPacketDataSerializer;
import com.mengcraft.playersql.internal.v1_13_2.PacketDataSerializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.handler.codec.base64.Base64;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.nio.charset.StandardCharsets;

public class DataSerializer {

    private static final IPacketDataSerializer.IFactory PACKET_DATA_SERIALIZER_FACTORY;

    static {
        switch (Bukkit.getServer().getClass().getPackage().getName()) {
            case "org.bukkit.craftbukkit.v1_12_R1":
                PACKET_DATA_SERIALIZER_FACTORY = buf -> new com.mengcraft.playersql.internal.v1_12.PacketDataSerializer(buf);
                break;
            case "org.bukkit.craftbukkit.v1_13_R1":
                PACKET_DATA_SERIALIZER_FACTORY = buf -> new com.mengcraft.playersql.internal.v1_13.PacketDataSerializer(buf);
                break;
            case "org.bukkit.craftbukkit.v1_13_R2":
                PACKET_DATA_SERIALIZER_FACTORY = buf -> new com.mengcraft.playersql.internal.v1_13_2.PacketDataSerializer(buf);
                break;
            default:
                PACKET_DATA_SERIALIZER_FACTORY = null;
                break;
        }
    }

    @SneakyThrows
    public static String serialize(ItemStack input) {
        if (PACKET_DATA_SERIALIZER_FACTORY == null) {
            return StreamSerializer.getDefault().serializeItemStack(input);
        }
        IPacketDataSerializer serializer = PACKET_DATA_SERIALIZER_FACTORY.create(PooledByteBufAllocator.DEFAULT.buffer());
        serializer.write(input);
        ByteBuf base64 = Base64.encode(serializer.buf());
        serializer.buf().release();
        CharSequence out = base64.readCharSequence(base64.readableBytes(), StandardCharsets.UTF_8);
        base64.release();
        return String.valueOf(out);
    }

    @SneakyThrows
    public static ItemStack deserialize(String input) {
        if (PACKET_DATA_SERIALIZER_FACTORY == null) {
            return StreamSerializer.getDefault().deserializeItemStack(input);
        }
        ByteBuf base64 = PooledByteBufAllocator.DEFAULT.buffer();
        base64.writeCharSequence(input, StandardCharsets.UTF_8);
        IPacketDataSerializer buf = PACKET_DATA_SERIALIZER_FACTORY.create(Base64.decode(base64));
        base64.release();
        ItemStack output = buf.readItemStack();
        buf.buf().release();
        return output;
    }

    public static boolean valid() {
        if (PACKET_DATA_SERIALIZER_FACTORY == null && Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
            return false;
        }
        return true;
    }
}
