package com.yimi.netutil;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;

import org.json.JSONObject;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by liuniu on 2017/12/14.
 */

public class OkHttpFactory {
    public static final String TAG = "OkHttpFactory";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType BYTES = MediaType.parse("application/actet-stream");
    public static final long MAX_SIZE = 1024 * 300;
    public static final int TIME_OUT_NUM = 20;
    public static final int DEBUG_TIME_OUT_NUM = 120;
    public static final int DEFAULT_TIMEOUT = 10;

    public static final int POST_DATA_TYPE_JSON = 1;
    public static final int POST_DATA_TYPE_FORM = 2;

    @IntDef({
            POST_DATA_TYPE_JSON,
            POST_DATA_TYPE_FORM,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface PostDataType {
    }

    public OkHttpClient netClient; // post
    public OkHttpClient cmsClient; // get

    private static volatile OkHttpFactory okhttpUtils;

    public static OkHttpFactory getInstance() {
        if (okhttpUtils == null) {
            synchronized (OkHttpFactory.class) {
                if (okhttpUtils == null) {
                    okhttpUtils = new OkHttpFactory();
                }
            }
        }
        return okhttpUtils;
    }

    private OkHttpFactory() {
        Context context = NetUtils.getContext();
        if (NetUtils.isDebug() && context != null) {
            Stetho.initialize(Stetho.newInitializerBuilder(context)
                    .enableDumpapp(Stetho.defaultDumperPluginsProvider(context))
                    .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(context))
                    .build());
        }
        OkHttpClient.Builder netBuilder = getOkHttpBuilder();
        // TODO: 2017/12/15  
        //验证证书指纹锁定,绑定host
        //netBuilder.certificatePinner(new CertificatePinner.Builder().add(host,pin).build());
        netClient = netBuilder.build();
        netBuilder.cache(new Cache(StorageUtils.getCacheStorage(), MAX_SIZE));
        cmsClient = netBuilder.build();
    }

    private OkHttpClient.Builder getOkHttpBuilder() {
        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
        if (NetUtils.isDebug()) {
            okHttpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
            okHttpClientBuilder.addNetworkInterceptor(new StethoInterceptor());
            okHttpClientBuilder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } else {
            okHttpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        }
        return okHttpClientBuilder;
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
    public static Request.Builder getJsonDataRequest(
            String function, Map params, Request.Builder request) {
        request.post(RequestBody.create(JSON, Utils.getJsonStringData(params)));
        addHttpHeadersWithJsonType(request, function);
        request.tag(function);
        return request;
    }

    /**
     * @param params {@code Map<key, value>}
     *               <P>transform the key/value to String internally by `String.valueOf()`
     */
    public static Request.Builder getFormDataRequest(
            String function, Map params, Request.Builder request) {
        FormBody.Builder formBodyBuilder = new FormBody.Builder();

        if (params != null && params.size() > 0) {
            String key, value;
            Map<?, ?> contentsTyped = (Map<?, ?>) params;
            for (Map.Entry<?, ?> entry : contentsTyped.entrySet()) {
                key = String.valueOf(entry.getKey());
                if (key == null) {
                    continue;
                }
                value = String.valueOf(entry.getValue());
                if (value == null) {
                    continue;
                }
                formBodyBuilder.add(key, value);
            }
        }

        FormBody formBody = formBodyBuilder.build();
        request.post(formBody);
        addHttpHeaders(request, function);
        request.tag(function);
        return request;
    }

    /**
     * get MultiPort request contains file and form data.
     *
     * @param function
     * @param fileKey
     * @param file
     * @param fileContentType such as: "image/*", "image/jpeg", "image/png"
     * @param params          {@code Map<key, value>},
     *                        transform the key/value to String internally by `String.valueOf()`
     * @param request
     * @return
     */
    public static Request.Builder getMultiPortRequest(
            String function, String fileKey, File file, String fileContentType,
            Map params, Request.Builder request) {
        MultipartBody.Builder multiRequestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        if (file != null && fileKey != null) {
            RequestBody body = RequestBody.create(MediaType.parse(fileContentType), file);
            multiRequestBody.addFormDataPart(fileKey, file.getName(), body);
        } else {
            Glog.e("getMultiPortRequest: fileKey or file null");
        }

        if (params != null) {
            String key, value;
            Map<?, ?> contentsTyped = (Map<?, ?>) params;
            for (Map.Entry<?, ?> entry : contentsTyped.entrySet()) {
                key = String.valueOf(entry.getKey());
                if (key == null) {
                    continue;
                }
                value = String.valueOf(entry.getValue());
                if (value == null) {
                    continue;
                }
                multiRequestBody.addFormDataPart(key, value);
            }
        }

        request.post(multiRequestBody.build());
        //addHttpHeaders(request, function);
        request.tag(function);
        return request;
    }

    /**
     * @param request
     * @param functionName
     */
    public static void addHttpHeadersWithJsonType(Request.Builder request, String functionName) {
        //// TODO: 2017/12/15 添加登录态，版本号等头部信息
        if (functionName != null && functionName.equals(HttpConstants.Upload_file)) {
            request.addHeader(HttpConstants.HEADER_CONTENT_TYPE, "application/octet-stream");
        } else {
            request.addHeader(HttpConstants.HEADER_ACCEPT, "application/json,*/*");
            request.addHeader(HttpConstants.HEADER_CONTENT_TYPE, "application/json");
        }
    }

    /**
     * @param request
     * @param functionName
     */
    public static void addHttpHeaders(Request.Builder request, String functionName) {
        //// TODO: 2017/12/15 添加登录态，版本号等头部信息
        if (functionName != null && functionName.equals(HttpConstants.Upload_file)) {
            request.addHeader(HttpConstants.HEADER_CONTENT_TYPE, "application/octet-stream");
        } else {
            request.addHeader(HttpConstants.HEADER_ACCEPT, "application/json,*/*");
            request.addHeader(HttpConstants.HEADER_CONTENT_TYPE, "application/json,*/*");
        }
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
    private Call doAsyncRequestWithJsonData(String function, Request.Builder request,
                                            Map params, NetCallback callback) {
        final OkHttpClient okHttpClient = OkHttpFactory.getInstance().netClient;
        request = getJsonDataRequest(function, params, request);
        Call call = okHttpClient.newCall(request.build());
        call.enqueue(callback);
        retryCallBack(call, okHttpClient, callback);
        return call;
    }

    /**
     * @param params {@code Map<key, value>}
     *               <P>transform the key/value to String internally by `String.valueOf()`
     */
    private Call doAsyncRequestWithFormData(
            String function, Request.Builder request, Map params, NetCallback callback) {
        final OkHttpClient okHttpClient = OkHttpFactory.getInstance().netClient;
        request = getFormDataRequest(function, params, request);
        Call call = okHttpClient.newCall(request.build());
        call.enqueue(callback);
        retryCallBack(call, okHttpClient, callback);
        return call;
    }

    /**
     * @param url
     * @param fileKey
     * @param file
     * @param fileContentType such as: "image/*", "image/jpeg", "image/png"
     * @param request
     * @param params          {@code Map<key, value>},
     *                        transform the key/value to String internally by `String.valueOf()`
     * @param callback
     * @return
     */
    private Call doAsyncPostFileWithFormData(
            String url, String fileKey, File file, String fileContentType, Request.Builder request,
            Map params, NetCallback callback) {
        final OkHttpClient okHttpClient = OkHttpFactory.getInstance().netClient;
        request = getMultiPortRequest(url, fileKey, file, fileContentType, params, request);
        Call call = okHttpClient.newCall(request.build());
        call.enqueue(callback);
        retryCallBack(call, okHttpClient, callback);
        return call;
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
    public Call postAsyncWithJsonData(
            String function, Map params, final NetCallback callback) {
        Request.Builder request = new Request.Builder()
                .url(ServerConnect.getInstance().getUrl(function));
        return doAsyncRequestWithJsonData(function, request, params, callback);
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
    public Call postAsyncWithJsonData(
            String function, Map params, Map<String, String> headers,
            final NetCallback callback) {
        Request.Builder request = new Request.Builder()
                .url(ServerConnect.getInstance().getUrl(function));
        // add headers
        addHeaders(request, headers);
        return doAsyncRequestWithJsonData(function, request, params, callback);
    }

    /**
     * @param function
     * @param params   {@code Map<key, value>},
     *                 transform the key/value to String internally by `String.valueOf()`
     * @param callback
     * @return
     */
    public Call postAsyncWithFormData(String function, Map params, final NetCallback callback) {
        Request.Builder request = new Request.Builder()
                .url(ServerConnect.getInstance().getUrl(function));
        return doAsyncRequestWithFormData(function, request, params, callback);
    }

    /**
     * @param function
     * @param params   {@code Map<key, value>},
     *                 transform the key/value to String internally by `String.valueOf()`
     * @param headers
     * @param callback
     * @return
     */
    public Call postAsyncWithFormData(
            String function, Map params, Map<String, String> headers, final NetCallback callback) {
        Request.Builder request = new Request.Builder()
                .url(ServerConnect.getInstance().getUrl(function));
        // add headers
        addHeaders(request, headers);
        return doAsyncRequestWithFormData(function, request, params, callback);
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
    public Call postFileAsyncWithFormData(
            String url, String fileKey, File file, String fileContentType,
            @Nullable Map params,
            @Nullable Map<String, String> headers, final NetCallback callback) {
        Request.Builder request = new Request.Builder()
                .url(ServerConnect.getInstance().getUrl(url));
        // add headers
        addHeaders(request, headers);
        return doAsyncPostFileWithFormData(
                url, fileKey, file, fileContentType, request, params, callback);
    }

    public <T> T postSync(String function, Map params, Class<T> clazz, @PostDataType int dataType) {
        OkHttpClient okHttpClient = OkHttpFactory.getInstance().netClient;
        Request.Builder request = new Request.Builder()
                .url(ServerConnect.getInstance().getUrl(function));

        // set content type
        switch (dataType) {
            case POST_DATA_TYPE_JSON:
                request = getJsonDataRequest(function, params, request);
                break;
            case POST_DATA_TYPE_FORM:
                request = getFormDataRequest(function, params, request);
                break;
            default:
                break;
        }

        try {
            T resObj;
            Call call = okHttpClient.newCall(request.build());
            Response response = call.execute();
            //byte[] bytes = response.body().bytes();
            resObj = Utils.transformResponseObj(response.body().string(), clazz, function);
            return resObj;
        } catch (Exception e) {
            Glog.e(TAG, "okhttp postSync error", e);
        }
        return null;
    }

    public Call postFile(File file, NetCallback callback) {
        OkHttpClient okHttpClient = OkHttpFactory.getInstance().netClient;
        String serverUrl = ServerConnect.getInstance().getUrl(HttpConstants.Upload_file);
        Uri.Builder uriBuilder = Uri.parse(serverUrl).buildUpon();
        /*Uri.Builder uriBuilder = Uri.parse(serverUrl).buildUpon()
                .appendQueryParameter("servId", serviceId);
        if (faceType != null) {
            uriBuilder.appendQueryParameter("faceType", faceType);
        }*/
        Request.Builder request = new Request.Builder().url(uriBuilder.build().toString());
        addHttpHeadersWithJsonType(request, null);
        request.post(RequestBody.create(BYTES, file));
        Call call = okHttpClient.newCall(request.build());
        call.enqueue(callback);
        retryCallBack(call, okHttpClient, callback);
        return call;
    }

    public Call getAsync(String url, NetCallback callback) {
        callback.isMtp = false;
        OkHttpClient okHttpClient = OkHttpFactory.getInstance().cmsClient;
        Request.Builder request = new Request.Builder()
                .url(url).get();
        request.addHeader("Cache-Control", "public, max-age=" + 0);
        Call call = okHttpClient.newCall(request.build());
        call.enqueue(callback);
        return call;
    }

    public <T> T getSync(String url, Class<T> clazz) {
        OkHttpClient okHttpClient = OkHttpFactory.getInstance().cmsClient;
        Request.Builder request = new Request.Builder().url(url).get();
        request.addHeader("Cache-Control", "public, max-age=" + 0);
        try {
            Response response = okHttpClient.newCall(request.build()).execute();
            //final T resObj = com.alibaba.fastjson.JSON.parseObject(response.body().string(), clazz);
            final T resObj = Utils.transformResponseObj(response.body().string(), clazz, url);
            return resObj;
        } catch (Exception e) {
            Glog.e(TAG, "okhttp getSync error", e);
        }
        return null;
    }

    public Response getSync(String url) throws Exception {
        OkHttpClient okHttpClient = OkHttpFactory.getInstance().cmsClient;
        Request.Builder request = new Request.Builder()
                .url(url).get();
        request.addHeader("Cache-Control", "public, max-age=" + 0);
        return okHttpClient.newCall(request.build()).execute();
    }

    private void retryCallBack(
            final Call call, final OkHttpClient okHttpClient, final NetCallback callback) {
        callback.setReTryCallListener(new NetCallback.ReTryCallListener() {
            @Override
            public void reTryCallListener(Call call, String bodyToString) {
                callback.isReTry = true;
                Request request = call.request();
                call = okHttpClient.newCall(request);
                call.enqueue(callback);
            }
        });
    }

    public static void updateMtpCookie(boolean isMtp, Response response) {
        if (isMtp) {//Mtp请求需要设置全局的cookie
            List<String> cookies = response.headers("Set-Cookie");
            if (cookies != null) {
                for (String cookie : cookies) {
                    if (!TextUtils.isEmpty(cookie) && cookie.startsWith("JSESSION")) {
                        // TODO: 2017/12/15 更新登录态 
                        Glog.d(TAG, "Set-Cookie is " + cookie);
                    }
                }
            }
        }
    }

    public static void checkResponseCertificate(
            boolean isMtp, Response response) throws CertificateException {
        if (isMtp
                && response.handshake() != null) {
            List<Certificate> certificateList = response.handshake().peerCertificates();
            if (certificateList != null && certificateList.size() > 0) {
                Certificate[] chain = certificateList.toArray(new Certificate[certificateList.size()]);
                // TODO: 2017/12/15  
                //校验证书指纹
                //CertificateValidator.getInstance().pin(chain);
            }
        }
    }

    private static void addHeaders(@NonNull Request.Builder request, Map<String, String> headers) {
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (!TextUtils.isEmpty(entry.getKey()) && !TextUtils.isEmpty(entry.getValue())) {
                    request.addHeader(entry.getKey(), entry.getValue());
                }
            }
        }
    }
}
