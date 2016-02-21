package com.mengcraft.playersql;

/**
 * Created on 16-1-2.
 */
public class PluginException extends RuntimeException {

    public PluginException(String s) {
        super(s);
    }

    public PluginException(String s, Exception e) {
        super(s, e);
    }

}
