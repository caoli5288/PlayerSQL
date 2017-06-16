package com.mengcraft.playersql.lib;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

import static com.mengcraft.playersql.PluginMain.nil;

/**
 * Created on 16-1-19.
 */
public final class JSONUtil {

    public static JSONArray parseArray(String in) {
        if (!nil(in)) {
            Object parsed = JSONValue.parse(in);
            if (parsed instanceof JSONArray) {
                return ((JSONArray) parsed);
            }
        }
        return new JSONArray();
    }

}
