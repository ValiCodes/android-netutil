package com.yimi.netutil;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.yimi.netutil.progressdialog.QProgressDialog;

import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLHandshakeException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by caiyu on 6/11/15.
 */
public abstract class NetCallback<T> implements Callback {
    private static Handler handler = new Handler(Looper.getMainLooper());
    private static final String TAG = "NetCallback";
    public QProgressDialog loadingDialog;
    public boolean isMtp = true;
    public T resObj;//接口序列化后的对象
    public String mResponse;

    public String functionName;//接口名称
    public ReTryCallListener reTryCallListener;

    public boolean isReTry;
    public long delayMillis;
    private Class<T> mClass;

    public NetCallback() {
    }

    public NetCallback(Class<T> clazz) {
        this.mClass = clazz;
    }

    @Override
    public final void onFailure(final Call request, final IOException e) {
        //超时等异常
        if (e != null) {
            if (e instanceof ConnectException || e instanceof SocketTimeoutException) {
                handleException(HttpConstants.STATUS_NETWORK_TIMEOUT, ServerConnect.getResultInfo(HttpConstants.STATUS_NETWORK_TIMEOUT));
            } else if ((e instanceof UnknownHostException)) {
                handleException(HttpConstants.STATUS_NETWORK_NOT_AVAILABLE, ServerConnect.getResultInfo(HttpConstants.STATUS_NETWORK_NOT_AVAILABLE));
            } else if (e instanceof SSLHandshakeException) {
                String message = e.getMessage();
                if (message != null && message.contains("ExtCertPathValidatorException")) {
                    handleException(900, "客户端时间异常，请修改本地时间后重试");
                } else {
                    handleException(404, "网络异常");
                }
            } else {
                handleException(404, "");
            }
        } else {
            handleException(404, "网络异常");
        }
    }

    @Override
    public final void onResponse(Call request, final Response response) throws IOException {
        try {
            if (response.code() == 200) {
                delayMillis = (response.receivedResponseAtMillis() - response.sentRequestAtMillis()) / 2;
                Class<T> entityClass = mClass;
                if (entityClass == null) {
                    entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
                }
                resObj = manageResponse(isMtp, request, response, entityClass, functionName);
                disposeResponse(handler, resObj, request, response.body().toString(), isReTry);
            } else {
                handleException(response.code(), ServerConnect.getHttpError(response.code()));
            }
        } catch (final Exception e) {
            //数据解析异常
            if (e instanceof CertificateException)
                handleException(response.code(), "数字证书异常，请联系客服人员!");
            else
                handleException(HttpConstants.STATUS_DATA_PARSE_ERROR, "数据解析异常  " + e.getMessage());
        } finally {
            if (response != null && response.body() != null) {
                response.close();
            }
        }
    }

    public void setReTryCallListener(ReTryCallListener reTryCallListener) {
        this.reTryCallListener = reTryCallListener;
    }

    public void handleResult(T resObj, String resultCode, String resultMsg, Call call, String bodyToString, boolean isReTry) {
        /*//统一处理错误码
        if (HttpConstants.RESPONSE_CODE_9000 == resultCode) {
            if (!isReTry) {
                doOnFailure(resultCode, resultMsg);
            } else {
                reTryCallListener.reTryCallListener(call, bodyToString);
            }
            return;
        }
*/
        doOnResponse(resObj);
       /* //resultCode为200 回调onResponse，非200回调onFailure
        if (HttpConstants.isSuccess(resultCode)) {
            doOnResponse(resObj);
        } else {
            doOnFailure(resultCode, resultMsg);
        }*/
    }

    private void handleException(final int resultCode, final String resultMessage) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                NetUtils.dismissDialog(loadingDialog);
                if (TextUtils.isEmpty(resultMessage)) {
                    //异常信息为空，默认为
                    doOnFailure(resultCode, ServerConnect
                            .getResultInfo(HttpConstants.STATUS_NETWORK_ERROR));
                } else {
                    doOnFailure(resultCode, resultMessage);
                }
            }
        });
    }


    public <T> T manageResponse(boolean isMtp, Call request, Response response, Class<T> entityClass, String functionName) throws Exception {
        T resObj = null;
        OkHttpFactory.checkResponseCertificate(isMtp, response);
        mResponse = response.body().string();
        String resString = mResponse;
        if (entityClass.equals(JSONObject.class)) {
            //org.json
            resObj = (T) new JSONObject(resString);
        } else if (entityClass.equals(String.class)) {
            //不处理String对象
            resObj = (T) resString;
        } else {
            //Gson
            long t1 = System.currentTimeMillis();
            //resObj = JSON.parseObject(resString, entityClass);
            resObj = new Gson().fromJson(resString, entityClass);
            Glog.d(TAG, "fastjsondecodetime function:" + functionName + "  time:" + (System.currentTimeMillis() - t1));
        }

        //数据解析为null报数据解析异常
        if (resObj == null) {
            throw new Exception();
        }

        OkHttpFactory.updateMtpCookie(isMtp, response);
        return resObj;
    }

    private void disposeResponse(Handler handler, final T resObj, final Call call, final String bodyToString, final boolean isReTry) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                NetUtils.dismissDialog(loadingDialog);
                if (!isMtp) {
                    doOnResponse(resObj);
                    return;
                }
                if (resObj instanceof ResponseBase) {
                    Class clazz = resObj.getClass();
                    Field[] fields = clazz.getFields();
                    ResponseBase responseBase = new ResponseBase();
                    try {
                        for (Field field : fields) {
                            if (field.getName().equals("code")) {
                                field.setAccessible(true);
                                responseBase.code = field.get(resObj) + "";
                            } else if (field.getName().equals("msg")) {
                                field.setAccessible(true);
                                responseBase.msg = (String) field.get(resObj);
                            }
                        }
                    } catch (Exception e) {
                        responseBase.msg = "数据解析失败" + e.getMessage();
                        responseBase.code = "404";
                    } finally {
                        handleResult(resObj, responseBase.code, responseBase.msg, call, bodyToString, isReTry);
                    }
                } else if (resObj instanceof JSONObject) {
                    JSONObject object = (JSONObject) resObj;
                    handleResult(resObj, object.optString("code"), object.optString("msg"), call, bodyToString, isReTry);
                } else {
                    //泛型对象未继承xxxx
                    doOnResponse(resObj);
                }
            }
        });
    }


    //开发版本中doOnFailure／doOnResponse会抛出网络接口处理异常，release版本加异常保护(需要做数据埋点)
    private void doOnFailure(int resultCode, String resultMsg) {
        NetUtils.removeCall(functionName);
        try {
            if (isRunning()) {
                onFailure(resultCode, resultMsg);
            } else {
                processRunningError();
            }
        } catch (Exception e) {
            if (NetUtils.isDebug()) {
                throw new RuntimeException(e);
            }
        } finally {
            onFinally();
        }
    }

    private void doOnResponse(T response) {
        NetUtils.removeCall(functionName);
        try {
            if (isRunning()) {
                onResponse(response);
            } else {
                processRunningError();
            }
        } catch (Exception e) {
            if (NetUtils.isDebug()) {
                throw new RuntimeException(e);
            }
        } finally {
            onFinally();
        }
    }


    /**
     * 重写判断是否需要判断view是否在运行
     *
     * @return
     */
    public boolean isRunning() {
        return true;
    }

    /**
     * 处理业务销毁的异常
     */
    public void processRunningError() {

    }

    public abstract void onFailure(int resultCode, String resultMsg);

    public abstract void onResponse(T response);

    /**
     * onResponse/onFailure 的统一操作可以移到onFinally中进行
     */
    public void onFinally() {

    }

    public interface ReTryCallListener {
        void reTryCallListener(Call call, String bodyToString);
    }

}