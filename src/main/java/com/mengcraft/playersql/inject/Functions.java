package com.mengcraft.playersql.inject;

import io.netty.channel.ChannelConfig;

public class Functions {

    public interface AutoReadSetter {

        ChannelConfig setAutoRead(boolean b);
    }
}
