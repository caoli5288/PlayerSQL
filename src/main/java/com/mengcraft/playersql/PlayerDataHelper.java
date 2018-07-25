package com.mengcraft.playersql;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mengcraft.playersql.lib.Compressor;
import com.mengcraft.playersql.lib.Decompressor;
import com.mengcraft.playersql.lib.VarInt;
import lombok.SneakyThrows;

import java.io.*;
import java.util.UUID;

public class PlayerDataHelper {

    @SneakyThrows
    public static byte[] encode(PlayerData dat) {
        ByteArrayDataOutput output = ByteStreams.newDataOutput();
        output.writeLong(dat.getUuid().getMostSignificantBits());
        output.writeLong(dat.getUuid().getLeastSignificantBits());
        output.writeDouble(dat.getHealth());
        output.writeInt(dat.getFood());
        output.writeInt(dat.getHand());
        output.writeInt(dat.getExp());
        write(output, dat.getInventory());
        write(output, dat.getArmor());
        write(output, dat.getChest());
        write(output, dat.getEffect());
        return Compressor.compress(output.toByteArray());
    }

    @SneakyThrows
    public static PlayerData decode(byte[] buf) {
        ByteArrayDataInput input = ByteStreams.newDataInput(Decompressor.decompress(buf));
        PlayerData dat = new PlayerData();
        dat.setUuid(new UUID(input.readLong(), input.readLong()));
        dat.setHealth(input.readDouble());
        dat.setFood(input.readInt());
        dat.setHand(input.readInt());
        dat.setExp(input.readInt());
        dat.setInventory(readString(input));
        dat.setArmor(readString(input));
        dat.setChest(readString(input));
        dat.setEffect(readString(input));
        return dat;
    }

    @SneakyThrows
    private static void write(DataOutput buf, String input) {
        if (input == null) {
            VarInt.writeUnsignedVarInt(buf, 0);
            return;
        }
        byte[] data = input.getBytes("utf8");
        VarInt.writeUnsignedVarInt(buf, data.length);
        buf.write(data);
    }

    @SneakyThrows
    private static String readString(DataInput buf) {
        long len = VarInt.readUnsignedVarInt(buf);
        if (len == 0) {
            return null;
        }
        byte[] readbuf = new byte[(int) len];
        buf.readFully(readbuf);
        return new String(readbuf, "utf8");
    }
}
