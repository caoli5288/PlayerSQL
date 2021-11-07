package com.github.caoli5288.playersql.bungee.protocol;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
public class DataSupply extends AbstractSqlPacket {

    private UUID id;
    private String group;
    private byte[] buf;

    @Override
    public ProtocolId getProtocol() {
        return ProtocolId.CONTENTS;
    }

    @Override
    protected void write(ByteArrayDataOutput out) {
        out.writeLong(id.getMostSignificantBits());
        out.writeLong(id.getLeastSignificantBits());
        out.writeUTF(group);
        out.writeInt(buf.length);
        out.write(buf);
    }

    @Override
    protected void read(ByteArrayDataInput from) {
        id = new UUID(from.readLong(), from.readLong());
        group = from.readUTF();
        buf = new byte[from.readInt()];
        from.readFully(buf);
    }
}
