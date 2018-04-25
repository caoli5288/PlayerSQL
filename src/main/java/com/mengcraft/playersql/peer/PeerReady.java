package com.mengcraft.playersql.peer;

import com.google.common.io.ByteArrayDataOutput;
import lombok.Data;

import java.util.UUID;

@Data
public class PeerReady extends IPacket {

    private UUID id;

    public PeerReady() {
        super(Protocol.PEER_READY);
    }

    @Override
    protected void read(ByteArrayDataOutput buf) {
        buf.writeLong(id.getMostSignificantBits());
        buf.writeLong(id.getLeastSignificantBits());
    }
}
