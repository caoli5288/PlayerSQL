package com.mengcraft.playersql.peer;

import com.google.common.io.ByteArrayDataOutput;
import lombok.Data;

import java.util.UUID;

@Data
public class DataRequest extends PlayerSqlProtocol {

    private UUID id;

    public DataRequest() {
        super(Protocol.DATA_REQUEST);
    }

    @Override
    protected void write(ByteArrayDataOutput buf) {
        buf.writeLong(id.getMostSignificantBits());
        buf.writeLong(id.getLeastSignificantBits());
    }
}
