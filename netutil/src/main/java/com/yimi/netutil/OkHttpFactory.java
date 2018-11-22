package com.yimi.netutil;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;

import java.io.File;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
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

    public OkHttpClient netClient;
    public OkHttpClient cmsClient;

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
            okHttpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS).writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS).readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
            okHttpClientBuilder.addNetworkInterceptor(new StethoInterceptor());
            okHttpClientBuilder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } else {
            okHttpClientBuilder.connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS).writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS).readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS);
        }
        return okHttpClientBuilder;
    }

    public static Request.Builder netRequest(String function, byte[] requestBody, Request.Builder request) {
        request.post(RequestBody.create(JSON, requestBody));
        addHttpHeaders(request, requestBody, function);
        request.tag(function);
        return request;
    }

    public static Request.Builder netRequestWithFormData(String function, Map requestBody, Request.Builder request) {
        FormBody.Builder formBodyBuilder = new FormBody.Builder();
        if (requestBody != null && requestBody.size() > 0) {
            for (Iterator iterator = requestBody.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<String, String> entry = (Map.Entry) iterator.next();
                formBodyBuilder.add(entry.getKey(), entry.getValue());
            }
        }
        FormBody formBody = formBodyBuilder.build();
        request.post(formBody);
        addHttpHeaders(request, function);
        request.tag(function);
        return request;
    }

    /**
     * @param request
     * @param requestBody
     * @param functionName
     */
    public static void addHttpHeaders(Request.Builder request, byte[] requestBody, String functionName) {
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

    private Call doAsyncRequest(
            String function, Request.Builder request,
            byte[] requestBody, NetCallback callback) {
        final OkHttpClient okHttpClient = OkHttpFactory.getInstance().netClient;
        request = netRequest(function, requestBody, request);
        Call call = okHttpClient.newCall(request.build());
        call.enqueue(callback);
        retryCallBack(call, okHttpClient, callback);
        return call;
    }

    private Call doAsyncRequestWithFormData(String function, Request.Builder request, Map requestBody, NetCallback callback) {
        final OkHttpClient okHttpClient = OkHttpFactory.getInstance().netClient;
        request = netRequestWithFormData(function, requestBody, request);
        Call call = okHttpClient.newCall(request.build());
        call.enqueue(callback);
        retryCallBack(call, okHttpClient, callback);
        return call;
    }

    public Call postAsync(String function, byte[] requestBody, final NetCallback callback) {
        Request.Builder request = new Request.Builder()
                .url(ServerConnect.getInstance().getUrl(function));
        return doAsyncRequest(function, request, requestBody, callback);
    }

    public Call postAsync(String function, byte[] requestBody, Map<String, String> headers,
                          final NetCallback callback) {
        Request.Builder request = new Request.Builder()
                .url(ServerConnect.getInstance().getUrl(function));
        // add headers
        addHeaders(request, headers);
        return doAsyncRequest(function, request, requestBody, callback);
    }

    public Call postAsyncWithFormData(String function, Map map, final NetCallback callback) {
        Request.Builder request = new Request.Builder()
                .url(ServerConnect.getInstance().getUrl(function));
        return doAsyncRequestWithFormData(function, request, map, callback);
    }

    public Call postAsyncWithFormData(
            String function, Map params, Map<String, String> headers, final NetCallback callback) {
        Request.Builder request = new Request.Builder()
                .url(ServerConnect.getInstance().getUrl(function));
        // add headers
        addHeaders(request, headers);
        return doAsyncRequestWithFormData(function, request, params, callback);
    }

    public <T> T postSync(String function, byte[] requestBody, Class<T> clazz) {
        OkHttpClient okHttpClient = OkHttpFactory.getInstance().netClient;
        Request.Builder request = new Request.Builder()
                .url(ServerConnect.getInstance().getUrl(function));
        request = netRequest(function, requestBody, request);
        request.post(RequestBody.create(JSON, requestBody));
        try {
            T resObj;
            Call call = okHttpClient.newCall(request.build());
            Response response = call.execute();
            byte[] bytes = response.body().bytes();
            resObj = new Gson().fromJson(response.body().string(), clazz);
            return resObj;
        } catch (Exception e) {
            Glog.e(TAG, "okhttp postSync error:" + e.getMessage());
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
        addHttpHeaders(request, null, null);
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
        Request.Builder request = new Request.Builder()
                .url(url).get();
        request.addHeader("Cache-Control", "public, max-age=" + 0);
        try {
            Response response = okHttpClient.newCall(request.build()).execute();
            //final T resObj = com.alibaba.fastjson.JSON.parseObject(response.body().string(), clazz);
            final T resObj = new Gson().fromJson(response.body().string(), clazz);
            return resObj;
        } catch (Exception e) {
            Glog.e(TAG, "okhttp getSync error:" + e.getMessage());
        }
        return null;
    }

    public Response getSync(String url) throws Exception{
        OkHttpClient okHttpClient = OkHttpFactory.getInstance().cmsClient;
        Request.Builder request = new Request.Builder()
                .url(url).get();
        request.addHeader("Cache-Control", "public, max-age=" + 0);
        return okHttpClient.newCall(request.build()).execute();
    }

    private void retryCallBack(final Call call, final OkHttpClient okHttpClient, final NetCallback callback) {
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

    public static void checkResponseCertificate(boolean isMtp, Response response) throws CertificateException {
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
