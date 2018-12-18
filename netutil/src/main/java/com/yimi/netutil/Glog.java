package com.yimi.netutil;

/**
 * Created by xiejingya on 2016/8/26.
 */

public class Glog {
    private final static String TAG = "NetUtil";
    public static boolean isV = true;
    public static boolean isD = true;
    public static boolean isI = true;
    public static boolean isW = true;
    public static boolean isE = true;

    public static void v(String tag, String s) {
        if (s == null) {
            s = "";
        }
        if (isV)
            android.util.Log.v(getUnitedTags(tag), s);
    }

    public static void v(String s) {
        if (s == null) {
            s = "";
        }
        if (isV)
            android.util.Log.v(TAG, s);
    }

    public static void v(String tag, String msg, Object... args) {
        if (msg == null) {
            msg = "";
        }
        if (isV) {
            if (args != null && args.length > 0) {
                msg = String.format(msg, args);
            }
            android.util.Log.v(getUnitedTags(tag), msg);
        }
    }

    public static void d(String tag, String s) {
        if (s == null) {
            s = "";
        }
        if (isD)
            android.util.Log.d(getUnitedTags(tag), s);
    }

    public static void d(String s) {
        if (s == null) {
            s = "";
        }
        if (isD) {
            android.util.Log.d(TAG, s);
        }

    }

    public static void d(String tag, String msg, Object... args) {
        if (msg == null) {
            msg = "";
        }
        if (isD) {
            if (args != null && args.length > 0) {
                msg = String.format(msg, args);
            }
            android.util.Log.d(getUnitedTags(tag), msg);
        }
    }

    public static void i(String tag, String s) {
        if (s == null) {
            s = "";
        }
        if (isI)
            android.util.Log.i(TAG + "-" + tag, s);
        android.util.Log.d(getUnitedTags(tag), s);

    }

    public static void i(String s) {
        if (s == null) {
            s = "";
        }
        if (isI)
            android.util.Log.i(TAG, s);
    }

    public static void i(String tag, String msg, Object... args) {
        if (msg == null) {
            msg = "";
        }
        if (isI) {
            if (args != null && args.length > 0) {
                msg = String.format(msg, args);
            }
            android.util.Log.i(getUnitedTags(tag), msg);
        }
    }

    public static void w(String tag, String s) {
        if (s == null) {
            s = "";
        }
        if (isW)
            android.util.Log.w(getUnitedTags(tag), s);
    }

    public static void w(String s) {
        if (s == null) {
            s = "";
        }
        if (isW)
            android.util.Log.w(TAG, s);
    }

    public static void e(String tag, String s) {
        if (s == null) {
            s = "";
        }
        if (isE)
            android.util.Log.e(getUnitedTags(tag), s);
    }

    public static void e(String s) {
        if (s == null) {
            s = "";
        }
        if (isE)
            android.util.Log.e(TAG, s);
    }

    public static void e(String tag, String msg, Object... args) {
        if (msg == null) {
            msg = "";
        }
        if (isE) {
            if (args != null && args.length > 0) {
                msg = String.format(msg, args);
            }
            android.util.Log.e(getUnitedTags(tag), msg);
        }
    }

    private static String getUnitedTags(String tag) {
        return TAG + "-" + tag;
    }
}
