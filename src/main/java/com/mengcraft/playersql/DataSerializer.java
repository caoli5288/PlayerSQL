package com.mengcraft.playersql;

import com.comphenix.protocol.utility.StreamSerializer;
import com.mengcraft.playersql.internal.IPacketDataSerializer;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.Base64;

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
        String data;
        try (IPacketDataSerializer serializer = PACKET_DATA_SERIALIZER_FACTORY.create(PooledByteBufAllocator.DEFAULT.buffer())) {
            serializer.write(input);
            data = Base64.getEncoder().encodeToString(serializer.readAll());
        }
        return data;
    }

    @SneakyThrows
    public static ItemStack deserialize(String input) {
        if (PACKET_DATA_SERIALIZER_FACTORY == null) {
            return StreamSerializer.getDefault().deserializeItemStack(input);
        }
        ItemStack output;
        try (IPacketDataSerializer serializer = PACKET_DATA_SERIALIZER_FACTORY.create(Unpooled.wrappedBuffer(Base64.getDecoder().decode(input)))) {
            output = serializer.readItemStack();
        }
        return output;
    }

    public static boolean valid() {
        if (PACKET_DATA_SERIALIZER_FACTORY == null && Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
            return false;
        }
        return true;
    }
}
