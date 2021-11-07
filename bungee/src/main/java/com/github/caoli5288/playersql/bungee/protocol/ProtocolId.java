package com.github.caoli5288.playersql.bungee.protocol;

import java.util.Objects;

public enum ProtocolId {

    READY,
    CONTENTS,
    REQUEST;

    public static AbstractSqlPacket ofPacket(ProtocolId id) {
        switch (id) {
            case READY:
                return new PeerReady();
            case CONTENTS:
                return new DataSupply();
            case REQUEST:
                return new DataRequest();
        }
        throw new IllegalStateException("unknown ProtocolId " + id);
    }
}
