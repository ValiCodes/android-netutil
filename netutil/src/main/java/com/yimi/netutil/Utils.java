package com.yimi.netutil;

import android.support.annotation.Nullable;

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


    /**
     * @param jsonMap recommend: {@code Map<String, Object>}
     *                <p>
     *                Supported Value Object: `null`, `NULL`, `String`,
     *                instanceof `JSONArray`, instanceof `JSONObject`,
     *                instanceof primitive wrapper type (`Boolean`, `Byte`, `Character`...),
     *                and the instanceof `Map` or `Collection` of those.
     *                Otherwise if the object is from a {@code java} package,
     *                use the result of {@code toString}.
     *                <P>Refs implementation: {@link JSONObject#wrap}
     */
    public static byte[] getJsonStringData(Map jsonMap) {
        byte[] result = null;
        if (jsonMap == null) {
            jsonMap = new HashMap<String, Object>();
        }
        try {
            //result = JSONObject.toJSONString(jsonMap).getBytes("utf-8");
            result = new JSONObject(jsonMap).toString().getBytes("utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @param responseStr
     * @param clazz support clazz type: `String`, `JSONObject`, and java bean.
     * @param functionName
     * @param <T>
     * @return
     * @throws Exception 上报数据解析异常
     */
    public static <T> T transformResponseObj(
            String responseStr, Class<T> clazz, @Nullable String functionName) throws Exception {
        T resObj = null;
        if (clazz.equals(JSONObject.class)) {
            resObj = (T) new JSONObject(responseStr);
        } else if (clazz.equals(String.class)) {
            // 不处理String对象
            resObj = (T) responseStr;
        } else {
            // Gson
            long t1 = System.currentTimeMillis();
            //resObj = JSON.parseObject(resString, clazz);
            resObj = new Gson().fromJson(responseStr, clazz);
            Glog.d(TAG, "parse json function: " + functionName
                    + ", time:" + (System.currentTimeMillis() - t1));
        }

        // 数据解析为null, 上报数据解析异常
        if (resObj == null) {
            throw new Exception();
        }

        return resObj;
    }
}
