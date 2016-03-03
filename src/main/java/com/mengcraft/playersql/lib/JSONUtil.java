package com.mengcraft.playersql.lib;

import org.json.simple.JSONArray;
import org.json.simple.JSONValue;

/**
 * Created on 16-1-19.
 */
public final class JSONUtil {

    public static JSONArray parseArray(String in, JSONArray normal) {
        if (in != null) {
            Object parsed = JSONValue.parse(in);
            if (parsed instanceof JSONArray) {
                return ((JSONArray) parsed);
            }
        }
        return normal;
    }

    public static final JSONArray EMPTY_ARRAY = new JSONArray();

}
