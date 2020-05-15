package com.mengcraft.playersql.inject;

import io.netty.channel.Channel;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ChannelProxies implements InvocationHandler {

    private final Map<String, Function<Object[], Object>> methods = new HashMap<>();
    private final Channel channel;
    private final HijConfig config;

    public ChannelProxies(Channel channel, HijConfig config) {
        this.channel = channel;
        this.config = config;
        methods.put("config", params -> this.config);
        methods.put("channel", params -> this.channel);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] params) throws Throwable {
        String name = method.getName();
        if (methods.containsKey(name)) {
            return methods.get(name).apply(params);
        }
        return method.invoke(channel, params);
    }

    public static Channel create(Channel delegate, HijConfig hijConfig) {
        return (Channel) Proxy.newProxyInstance(ChannelProxies.class.getClassLoader(), array(Channel.class, IWrappedChannel.class), new ChannelProxies(delegate, hijConfig));
    }

    private static Class<?>[] array(Class<?>... classes) {
        return classes;
    }

    public interface IWrappedChannel {

        Channel channel();
    }
}
