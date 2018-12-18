package com.yimi.netutil;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Utils {

    private static final String TAG = "Utils";

    public static <T> T getObjFromJson(String json, Class<T> classOfT) {
        try {
            return new Gson().fromJson(json, classOfT);
        } catch (Exception e) {
            Glog.e(TAG, "getObjFromJson error", e);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * return "" if param string is null, otherwise return itself.
     *
     * @param string
     * @return
     */
    public static String notNull(String string) {
        if (string == null) {
            return "";
        }
        return string;
    }

    public static byte[] getJsonStringData(Map jsonMap) {
        byte[] result = null;
        if (jsonMap == null) {
            jsonMap = new HashMap();
        }
        try {
            //result = JSONObject.toJSONString(jsonMap).getBytes("utf-8");
            result = new JSONObject(jsonMap).toString().getBytes("utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
