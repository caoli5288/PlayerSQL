package com.mengcraft.playersql.peer;

import com.google.common.io.ByteArrayDataOutput;
import lombok.Data;

import java.util.UUID;

@Data
public class DataSupply extends IPacket {

    private UUID id;
    private String group;
    private byte[] buf;

    public DataSupply() {
        super(Protocol.DATA_BUF);
    }

    @Override
    protected void write(ByteArrayDataOutput out) {
        out.writeLong(id.getMostSignificantBits());
        out.writeLong(id.getLeastSignificantBits());
        out.writeUTF(group);
        out.writeInt(buf.length);
        out.write(buf);
    }
}
