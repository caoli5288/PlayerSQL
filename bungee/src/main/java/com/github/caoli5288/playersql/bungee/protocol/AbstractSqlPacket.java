package com.github.caoli5288.playersql.bungee.protocol;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.SneakyThrows;

public abstract class AbstractSqlPacket {

    public abstract ProtocolId getProtocol();

    protected abstract void write(ByteArrayDataOutput to);

    protected abstract void read(ByteArrayDataInput from);

    @SneakyThrows
    public byte[] encode() {
        ByteArrayDataOutput buf = ByteStreams.newDataOutput();
        buf.writeByte(getProtocol().ordinal());
        write(buf);
        return buf.toByteArray();
    }

    public static AbstractSqlPacket decode(byte[] input) {
        ByteArrayDataInput buf = ByteStreams.newDataInput(input);
        ProtocolId protocolId = ProtocolId.values()[buf.readByte()];
        AbstractSqlPacket packet = ProtocolId.ofPacket(protocolId);
        packet.read(buf);
        return packet;
    }
}
