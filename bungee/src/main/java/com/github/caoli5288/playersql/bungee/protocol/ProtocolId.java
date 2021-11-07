package com.github.caoli5288.playersql.bungee.protocol;

import org.jetbrains.annotations.NotNull;

public enum ProtocolId {

    READY,
    CONTENTS,
    REQUEST;

    @NotNull
    public static AbstractSqlPacket ofPacket(@NotNull ProtocolId id) {
        switch (id) {
            case READY:
                return new PeerReady();
            case CONTENTS:
                return new DataSupply();
            case REQUEST:
                return new DataRequest();
        }
        throw new EnumConstantNotPresentException(ProtocolId.class, "Unknown " + id);
    }
}
