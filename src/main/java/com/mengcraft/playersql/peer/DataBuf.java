package com.mengcraft.playersql.peer;

import com.google.common.io.ByteArrayDataOutput;
import lombok.Data;

import java.util.UUID;

@Data
public class DataBuf extends IPacket {

    private UUID id;
    private byte[] buf;

    public DataBuf() {
        super(Protocol.DATA_BUF);
    }

    @Override
    protected void read(ByteArrayDataOutput out) {
        out.writeLong(id.getMostSignificantBits());
        out.writeLong(id.getLeastSignificantBits());
        out.writeInt(buf.length);
        out.write(buf);
    }
}
