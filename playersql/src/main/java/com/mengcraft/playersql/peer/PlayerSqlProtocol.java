package com.mengcraft.playersql.peer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import lombok.SneakyThrows;

import java.util.Objects;
import java.util.UUID;

public abstract class PlayerSqlProtocol {

    public static final String NAMESPACE = "playersql:main";
    public static final String MAGIC_KICK_MESSAGE = "playersql_magic_kick";

    private final Protocol protocol;

    public PlayerSqlProtocol(Protocol protocol) {
        this.protocol = Objects.requireNonNull(protocol);
    }

    public Protocol getProtocol() {
        return protocol;
    }

    protected abstract void write(ByteArrayDataOutput buf);

    @SneakyThrows
    public byte[] encode() {
        ByteArrayDataOutput buf = ByteStreams.newDataOutput();
        buf.writeByte(protocol.ordinal());
        write(buf);
        return buf.toByteArray();
    }

    public static PlayerSqlProtocol decode(byte[] input) {
        ByteArrayDataInput buf = ByteStreams.newDataInput(input);
        Protocol protocol = Protocol.values()[buf.readByte()];
        return protocol.decode(buf);
    }

    public enum Protocol {

        PEER_READY {
            public PlayerSqlProtocol decode(ByteArrayDataInput input) {
                PeerReady pk = new PeerReady();
                pk.setId(new UUID(input.readLong(), input.readLong()));
                return pk;
            }
        },

        DATA_CONTENTS {
            public PlayerSqlProtocol decode(ByteArrayDataInput input) {
                DataSupply pk = new DataSupply();
                pk.setId(new UUID(input.readLong(), input.readLong()));
                pk.setGroup(input.readUTF());
                byte[] buf = new byte[input.readInt()];
                input.readFully(buf);
                pk.setBuf(buf);
                return pk;
            }
        },

        DATA_REQUEST {
            public PlayerSqlProtocol decode(ByteArrayDataInput input) {
                DataRequest pk = new DataRequest();
                pk.setId(new UUID(input.readLong(), input.readLong()));
                return pk;
            }
        };

        public PlayerSqlProtocol decode(ByteArrayDataInput input) {
            throw new AbstractMethodError("decode");
        }
    }
}
