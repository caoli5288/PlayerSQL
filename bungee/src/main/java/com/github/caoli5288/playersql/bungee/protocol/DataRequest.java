package com.github.caoli5288.playersql.bungee.protocol;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
public class DataRequest extends AbstractSqlPacket {

    private UUID id;

    @Override
    public ProtocolId getProtocol() {
        return ProtocolId.REQUEST;
    }

    @Override
    protected void write(ByteArrayDataOutput buf) {
        buf.writeLong(id.getMostSignificantBits());
        buf.writeLong(id.getLeastSignificantBits());
    }

    @Override
    protected void read(ByteArrayDataInput from) {
        id = new UUID(from.readLong(), from.readLong());
    }
}
