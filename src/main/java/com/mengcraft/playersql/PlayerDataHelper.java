package com.mengcraft.playersql;

import lombok.SneakyThrows;

import java.io.*;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class PlayerDataHelper {

    @SneakyThrows
    public static byte[] encode(PlayerData dat) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        GZIPOutputStream zip = new GZIPOutputStream(buf);
        DataOutput output = new DataOutputStream(zip);

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

        zip.close();

        return buf.toByteArray();
    }

    @SneakyThrows
    public static PlayerData decode(byte[] buf) {
        DataInput input = new DataInputStream(new GZIPInputStream(new ByteArrayInputStream(buf)));
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
            buf.writeBoolean(false);
            return;
        }
        buf.writeBoolean(true);
        buf.writeUTF(input);
    }

    @SneakyThrows
    private static String readString(DataInput buf) {
        if (buf.readBoolean()) {
            return buf.readUTF();
        }
        return null;
    }
}
