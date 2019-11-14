package com.mengcraft.playersql.inject;

import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
public class HijChannel implements Channel, Excludes.ConfigGetter {

    @Delegate(excludes = Excludes.ConfigGetter.class)
    private final Channel delegate;
    private final HijConfig hijConfig;

    @Override
    public ChannelConfig config() {
        return hijConfig;
    }

    public Channel delegate() { return delegate; }
}
