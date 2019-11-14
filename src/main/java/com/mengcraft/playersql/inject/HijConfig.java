package com.mengcraft.playersql.inject;

import io.netty.channel.ChannelConfig;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

@RequiredArgsConstructor
public class HijConfig implements ChannelConfig, Excludes.AutoReadSetter {

    @Delegate(excludes = Excludes.AutoReadSetter.class)
    private final ChannelConfig delegate;

    @Override
    public ChannelConfig setAutoRead(boolean b) {
        System.out.println(String.format("HijConfig.setAutoRead(%s)", b));
        return this;
    }
}
