package com.mengcraft.playersql.inject;

import io.netty.channel.ChannelConfig;

public class Excludes {

    public interface ConfigGetter {

        ChannelConfig config();
    }

    public interface AutoReadSetter {

        ChannelConfig setAutoRead(boolean b);
    }
}
