package com.yimi.netutil;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import com.yimi.netutil.progressdialog.QProgressDialog;

import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Response;

/**
 * Created by liuniu on 2017/12/15.
 */

public class NetUtils {
    private static final String TAG = "NetUtil";
    public static final String Net_Wifi = "wifi";
    public static final String Net_UnKnown = "unknown";

    public static HashMap<String, Call> map = new HashMap<>();

    private static WeakReference<Context> sContextRef;

    private static volatile boolean isInited;

    private static boolean isDebug;

    public synchronized static void addCall(String url, Call call) {
        map.put(url, call);
    }

    public synchronized static Call getCall(String url) {
        if (!TextUtils.isEmpty(url) && isExist(url)) {
            return map.get(url);
        } else {
            return null;
        }
    }

    public synchronized static void removeCall(String url) {
        if (!TextUtils.isEmpty(url) && isExist(url)) {
            map.remove(url);
        }
    }

    public synchronized static boolean isExist(String url) {
        return map.containsKey(url);
    }

    /**
     * @param params recommend: {@code Map<String, Object>}
     *               <p>
     *               Supported Value Object: `null`, `NULL`, `String`,
     *               instanceof `JSONArray`, instanceof `JSONObject`,
     *               instanceof primitive wrapper type (`Boolean`, `Byte`, `Character`...),
     *               and the instanceof `Map` or `Collection` of those.
     *               Otherwise if the object is from a {@code java} package,
     *               use the result of {@code toString}.
     *               <P>Refs implementation: {@link JSONObject#wrap}
     */
    public static Call postWithJsonData(
            String function, @Nullable Map params, NetCallback callback) {
        callback.functionName = function;
        return OkHttpFactory.getInstance().postAsyncWithJsonData(function, params, callback);
    }

    /**
     * @param params recommend: {@code Map<String, Object>}
     *               <p>
     *               Supported Value Object: `null`, `NULL`, `String`,
     *               instanceof `JSONArray`, instanceof `JSONObject`,
     *               instanceof primitive wrapper type (`Boolean`, `Byte`, `Character`...),
     *               and the instanceof `Map` or `Collection` of those.
     *               Otherwise if the object is from a {@code java} package,
     *               use the result of {@code toString}.
     *               <P>Refs implementation: {@link JSONObject#wrap}
     */
    public static Call postWithJsonData(
            String function, @Nullable Map params,
            Map<String, String> headers, NetCallback callback) {
        callback.functionName = function;
        return OkHttpFactory.getInstance().postAsyncWithJsonData(function, params, headers, callback);
    }

    /**
     * @param function
     * @param params   {@code Map<key, value>},
     *                 transform the key/value to String internally by `String.valueOf()`
     * @param callback
     * @return
     */
    public static Call postWithFormData(
            String function, @Nullable Map params, NetCallback callback) {
        callback.functionName = function;
        return OkHttpFactory.getInstance().postAsyncWithFormData(function, params, callback);
    }

    /**
     * @param function
     * @param params   {@code Map<key, value>},
     *                 transform the key/value to String internally by `String.valueOf()`
     * @param headers
     * @param callback
     * @return
     */
    public static Call postWithFormData(String function, @Nullable Map params,
                                        Map<String, String> headers, NetCallback callback) {
        callback.functionName = function;
        return OkHttpFactory.getInstance()
                .postAsyncWithFormData(function, params, headers, callback);
    }

    /**
     * @param url
     * @param fileKey
     * @param file
     * @param fileContentType such as: "image/*", "image/jpeg", "image/png"
     * @param params          {@code Map<key, value>},
     *                        transform the key/value to String internally by `String.valueOf()`
     * @param headers
     * @param callback
     * @return
     */
    public static Call postFileWithFormData(
            String url, String fileKey, File file, String fileContentType,
            @Nullable Map params, Map<String, String> headers, NetCallback callback) {
        return OkHttpFactory.getInstance().postFileAsyncWithFormData(
                url, fileKey, file, fileContentType, params, headers, callback);
    }

    public static void postFile(File file, NetCallback callback) {
        callback.functionName = HttpConstants.Upload_file;
        OkHttpFactory.getInstance().postFile(file, callback);
    }

    public static void postFileWithDialog(
            Activity activity, String message, String serviceId, String faceType, File file,
            NetCallback callback) {
        callback.functionName = HttpConstants.Upload_file;

        if (TextUtils.isEmpty(message)) {
            message = "";
        }

        QProgressDialog dialog = null;
        if (activity != null && !activity.isFinishing()) {
            dialog = getLoadingDialog(activity, message);
            callback.loadingDialog = dialog;
        }
        final Call call = OkHttpFactory.getInstance().postFile(file, callback);
        if (dialog != null) {
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (!call.isCanceled()) {
                        OkHttpFactory.getInstance().netClient.dispatcher()
                                .executorService().execute(new Runnable() {
                            @Override
                            public void run() {
                                call.cancel();
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * 调用网络请求的同步方法，不能在主线程使用
     *
     * @param params recommend: {@code Map<String, Object>}
     *               <p>
     *               Supported Value Object: `null`, `NULL`, `String`,
     *               instanceof `JSONArray`, instanceof `JSONObject`,
     *               instanceof primitive wrapper type (`Boolean`, `Byte`, `Character`...),
     *               and the instanceof `Map` or `Collection` of those.
     *               Otherwise if the object is from a {@code java} package,
     *               use the result of {@code toString}.
     *               <P>Refs implementation: {@link JSONObject#wrap}
     */
    public static <T> T postSyncWithJsonData(
            String function, @Nullable Map params, Class<T> clazz) {
        return OkHttpFactory.getInstance().postSync(
                function, params, clazz, OkHttpFactory.POST_DATA_TYPE_JSON);
    }

    /**
     * @param params {@code Map<key, value>}
     *               <P>transform the key/value to String internally by `String.valueOf()`
     */
    public static <T> T postSyncWithFormData(
            String function, @Nullable Map params, Class<T> clazz) {
        return OkHttpFactory.getInstance().postSync(
                function, params, clazz, OkHttpFactory.POST_DATA_TYPE_FORM);
    }

    /**
     * Cms相关请求(异步请求，可在主线程调用)
     *
     * @param url
     * @param callback
     */
    public static Call getCms(String url, NetCallback callback) {
        callback.isMtp = false;
        return OkHttpFactory.getInstance().getAsync(url, callback);
    }

    /**
     * Cms相关请求(同步请求)
     *
     * @param url
     */
    public static Response getCmsSync(String url) throws Exception {
        return OkHttpFactory.getInstance().getSync(url);
    }

    /**
     * Cms相关请求(异步请求，可在主线程调用)
     *
     * @param url
     * @param callback
     */
    public static String getCmsWithParamsAndUrl(
            String url, HashMap<String, String> map, NetCallback callback) {
        callback.isMtp = false;
        String mUrl = getUrlWithMap(url, map);
        OkHttpFactory.getInstance().getAsync(mUrl, callback);
        return mUrl;
    }

    /**
     * Cms相关请求(异步请求，可在主线程调用)
     *
     * @param url
     * @param callback
     */
    public static String getCmsWithParamsAndUrlAnddialog(
            Activity activity, String message, String url, HashMap<String, String> map,
            NetCallback callback) {
        if (activity != null && !activity.isFinishing()) {
            QProgressDialog dialog = getLoadingDialog(activity, message);
            callback.loadingDialog = dialog;
            return getCmsWithParamsAndUrl(url, map, callback);
        } else {
            return getUrlWithMap(url, map);
        }
    }

    /**
     * Cms相关请求(异步请求，可在主线程调用)
     *
     * @param url      host
     * @param callback
     */
    public static Call getCmsWithParams(String url, Map<String, String> map, NetCallback callback) {
        callback.isMtp = false;
        String mUrl = getUrlWithMap(url, map);
        return OkHttpFactory.getInstance().getAsync(mUrl, callback);
    }

    /**
     * Cms相关请求(异步请求，可在主线程调用)
     * RemoveRequest.remove()可以取消请求
     *
     * @param url      host
     * @param callback
     */
    public static RemoveRequest getRemoveRequestCmsWithParams(
            String url, Map<String, String> map, NetCallback callback) {
        callback.isMtp = false;
        String mUrl = getUrlWithMap(url, map);
        Call call = OkHttpFactory.getInstance().getAsync(mUrl, callback);
        return new RemoveRequest(call, OkHttpFactory.getInstance().cmsClient);
    }

    /**
     * Cms相关请求(异步请求，可在主线程调用)
     * 根据host，来处理重复请求
     *
     * @param url      host
     * @param callback
     */
    public static void getRemoveCmsWithParams(
            String url, Map<String, String> map, NetCallback callback) {
        callback.isMtp = false;
        callback.functionName = url;
        String mUrl = getUrlWithMap(url, map);
        if (isExist(url)) {
            new RemoveRequest(getCall(url), OkHttpFactory.getInstance().cmsClient).remove();
            removeCall(url);
        }
        Call call = OkHttpFactory.getInstance().getAsync(mUrl, callback);
        addCall(url, call);
    }

    /**
     * Cms相关请求(异步请求，可在主线程调用)
     *
     * @param url      host
     * @param callback
     */
    public static void getCmsWithParamsAndDialog(
            Activity activity, String message, String url, Map<String, String> map,
            NetCallback callback) {
        if (activity != null && !activity.isFinishing()) {
            QProgressDialog dialog = getLoadingDialog(activity, message);
            callback.loadingDialog = dialog;
            final Call call = getCmsWithParams(url, map, callback);
            cancelCall(dialog, call);
        }
    }

    private static void cancelCall(Dialog dialog, final Call call) {
        if (dialog != null) {
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (!call.isCanceled()) {
                        OkHttpFactory.getInstance().cmsClient.dispatcher()
                                .executorService().execute(new Runnable() {
                            @Override
                            public void run() {
                                call.cancel();
                            }
                        });
                    }
                }
            });
        }
    }

    /**
     * Cms相关请求(异步请求，可在主线程调用)
     *
     * @param url      host
     * @param callback
     */
    public static Call getCmsWithParamsAndFunc(
            String url, String function, Map<String, String> map, NetCallback callback) {
        callback.isMtp = false;
        String mUrl = getUrlWithMap(url + function, map);
        return OkHttpFactory.getInstance().getAsync(mUrl, callback);
    }

    /**
     * Cms相关请求(异步请求，可在主线程调用)
     *
     * @param url      host
     * @param callback
     */
    public static void getCmsWithParamsAndFuncAndDialog(
            Activity activity, String message, String url, String function,
            Map<String, String> map, NetCallback callback) {
        if (activity != null && !activity.isFinishing()) {
            QProgressDialog dialog = getLoadingDialog(activity, message);
            callback.loadingDialog = dialog;
            final Call call = getCmsWithParamsAndFunc(url, function, map, callback);
            cancelCall(dialog, call);
        }
    }

    public static String getUrlWithMap(String url, Map<String, String> map) {
        String mUrl = url;
        if (!TextUtils.isEmpty(mUrl) && null != map && map.size() > 0) {
            Iterator<Map.Entry<String, String>> entries = map.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, String> entry = entries.next();
                mUrl = getWithParamUrl(mUrl, entry.getKey(), entry.getValue());
            }
        }
        Glog.d("getUrlWithMap", mUrl);
        return mUrl;
    }

    public static String getWithParamUrl(String url, String key, String value) {
        if (TextUtils.isEmpty(url)) return null;
        if ((url.startsWith("http:") || url.startsWith("https:"))
                && !TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
            if (containsParams(url)) {
                url += "&";
            } else {
                url += "?";
            }
            url += (key + "=" + value);
        } else {
            Glog.e("getWithParamUrl", "url = " + url
                    + "\n key = " + key + "\n value = " + value);
        }
        Glog.d("getWithParamUrl", "url is " + url);
        return url;
    }

    public static boolean containsParams(String url) {
        try {
            URL urlParam = new URL(url);
            return !TextUtils.isEmpty(urlParam.getQuery());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static void getCmsWithDialog(Activity activity, String message,
                                        String url, NetCallback callback) {
        if (activity != null && !activity.isFinishing()) {
            QProgressDialog dialog = getLoadingDialog(activity, message);
            callback.loadingDialog = dialog;
            final Call call = getCms(url, callback);
            cancelCall(dialog, call);
        }
    }

    public static <T> T getCmsSync(String url, Class<T> clazz) {
        return OkHttpFactory.getInstance().getSync(url, clazz);
    }

    /**
     * @param params recommend: {@code Map<String, Object>}
     *               <p>
     *               Supported Value Object: `null`, `NULL`, `String`,
     *               instanceof `JSONArray`, instanceof `JSONObject`,
     *               instanceof primitive wrapper type (`Boolean`, `Byte`, `Character`...),
     *               and the instanceof `Map` or `Collection` of those.
     *               Otherwise if the object is from a {@code java} package,
     *               use the result of {@code toString}.
     *               <P>Refs implementation: {@link JSONObject#wrap}
     */
    private static void postJsonDataWithDialog(
            Activity activity, String message, String function, Map params,
            NetCallback callback) {
        callback.functionName = function;
        if (TextUtils.isEmpty(message)) {
            message = "";
        }

        QProgressDialog dialog = null;
        if (activity != null && !activity.isFinishing()) {
            dialog = getLoadingDialog(activity, message);
            callback.loadingDialog = dialog;
        }
        final Call call = postWithJsonData(function, params, callback);
        if (dialog != null) {
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (!call.isCanceled()) {
                        OkHttpFactory.getInstance().netClient.dispatcher()
                                .executorService().execute(new Runnable() {
                            @Override
                            public void run() {
                                call.cancel();
                            }
                        });
                    }
                }
            });
        }
    }


    private static QProgressDialog getLoadingDialog(Context context, String dialogMessage) {
        if (context == null) {
            return null;
        }

        QProgressDialog progressDialog = new QProgressDialog(context, R.style.CustomDialog) {
            @Override
            public boolean onKeyDown(int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    this.cancel();
                }
                return super.onKeyDown(keyCode, event);
            }
        };
        progressDialog.setMessage(dialogMessage);
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        progressDialog.show();
        return progressDialog;
    }

    public static String getNetworkTypeName() {
        if (NetChangeListenTool.getNetworkInfo() != null) {
            return NetChangeListenTool.getNetworkInfo().getNetworkType();
        } else {
            return getNetworkTypeName(null);
        }
    }

    /**
     * 获取网络类型
     *
     * @param
     * @return 网络type:wifi, 2G, 3G, 4G,notavailable
     */
    public static String getNetworkTypeName(NetworkInfo activeNetworkInfo) {
        final String[] resultArray = {Net_UnKnown, Net_Wifi, "2G", "3G", "4G"};
        String resultString = resultArray[0];
        if (activeNetworkInfo == null) {
            Context context = getContext();
            if (context == null) {
                return resultArray[0];
            }
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            }
        }
        boolean isAvailable = activeNetworkInfo != null
                && activeNetworkInfo.isConnected();
        if (isAvailable) {
            int type = activeNetworkInfo.getType();
            int subType = activeNetworkInfo.getSubtype();
            String subTypeName = activeNetworkInfo.getSubtypeName();

            if (type == ConnectivityManager.TYPE_WIFI) {
                resultString = resultArray[1];
            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                switch (subType) {
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        resultString = resultArray[2];//2G
                        break;
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        resultString = resultArray[3];//3G
                        break;
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        resultString = resultArray[4];//4G
                        break;
                    default:
                        resultString = subTypeName;
                        break;
                }
            }
        }
        return resultString;
    }

    /**
     * 获取网络是否可用
     *
     * @return
     */
    public static boolean isNetworkAvailable() {
        NetChangeListenTool.QiyiNetworkInfo qiyiNetworkInfo = NetChangeListenTool.getNetworkInfo();
        if (qiyiNetworkInfo != null) {
            return qiyiNetworkInfo.isAvailable();
        } else {
            Context context = getContext();
            if (context == null) {
                return false;
            }
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = null;
            if (connectivityManager != null) {
                activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            }
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }

    /**
     * 获取wifi是否连接
     *
     * @return
     */
    public static boolean isWifiConnected() {
        NetChangeListenTool.QiyiNetworkInfo qiyiNetworkInfo = NetChangeListenTool.getNetworkInfo();
        if (qiyiNetworkInfo != null) {
            return Net_Wifi.equals(qiyiNetworkInfo.getNetworkType());
        } else {
            return Net_Wifi.equals(getNetworkTypeName());
        }
    }

    public static void dismissDialog(Dialog dialog) {
        try {
            if (dialog != null && dialog.getWindow() != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (Exception e) {
        }
    }

    public static void init(Context context, boolean debug) {
        Log.d(TAG, "NetUtil Version: "
                + com.yimi.netutil.BuildConfig.VERSION_NAME
                + " (" + com.yimi.netutil.BuildConfig.VERSION_CODE + ")");
        if (isInited) {
            Glog.e("initialized already");
            return;
        }
        if (context == null) {
            Glog.e("context is null");
            return;
        }
        sContextRef = new WeakReference<Context>(context.getApplicationContext());
        isDebug = debug;
        isInited = true;
    }

    public static Context getContext() {
        Context context = null;
        if (sContextRef != null) {
            context = sContextRef.get();
        }
        if (context == null) {
            Glog.e("context is null, have you called 'NetUtil.init()' before?");
        }
        return context;
    }

    public static void setContext(Context context) {
        if (context == null) {
            sContextRef = new WeakReference<Context>(null);
        } else {
            sContextRef = new WeakReference<Context>(context.getApplicationContext());
        }
    }

    public static boolean isDebug() {
        return isDebug;
    }

    public static void setDebug(boolean debug) {
        isDebug = debug;
    }

    /**
     * Cancel all calls currently enqueued or executing. Includes calls executed both {@linkplain
     * Call#execute() synchronously} and {@linkplain Call#enqueue asynchronously}.
     */
    public static void cancelAll() {
        try {
            // cancel get
            OkHttpFactory.getInstance().cmsClient.dispatcher().cancelAll();
            // cancel post
            OkHttpFactory.getInstance().netClient.dispatcher().cancelAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
