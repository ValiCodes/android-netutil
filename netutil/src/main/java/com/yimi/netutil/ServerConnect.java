package com.yimi.netutil;

/**
 * Created by liuniu on 2017/12/14.
 */

public class ServerConnect {

    private static ServerConnect instance;
    private String PING_BACK_API = "http://msg.qy.net/v5/yx/yxfc";


    public static ServerConnect getInstance() {
        if (instance == null) {
            synchronized (ServerConnect.class) {
                if (instance == null) {
                    instance = new ServerConnect();
                }
            }
        }
        return instance;
    }

    /**
     * 数据投递URL
     *
     * @return
     */
    public String getPostDataUrl() {
        return PING_BACK_API;
    }


    private ServerConnect() {
        if (NetUtils.isDebug()) {
            PING_BACK_API = "https://msg.qy.net/v5/yx/yxfc";
        } else {
            PING_BACK_API = "https://msg.qy.net/v5/yx/yxfc";
        }
    }

    public String getUrl(String function) {
        String url = function;
        switch (function) {
            case HttpConstants.ping_back:
                url = PING_BACK_API;
                break;
            case HttpConstants.Upload_file:
                break;
            default:
                break;
        }
        return url;
    }

    /**
     * 获取反馈文本信息
     *
     * @param resultCode
     * @return
     */
    public static String getResultInfo(int resultCode) {
        switch (resultCode) {
            case HttpConstants.STATUS_NETWORK_NOT_AVAILABLE:
                return "当前网络不可用，请检查网络设置后再试";
            case HttpConstants.STATUS_NETWORK_TIMEOUT:
                return "网络连接超时，请稍后再试";
            case HttpConstants.STATUS_SYSTEM_ERROR:
                return "系统异常，请稍后再试";
            case HttpConstants.STATUS_NETWORK_ERROR:
                return "网络连接异常请重试";
            default:
                return "";
        }
    }

    public static String getHttpError(int responseCode) {
        String resultMessage = "";
        switch (responseCode) {
            case HttpConstants.SC_MOVED_TEMPORARILY:
            case HttpConstants.SC_METHOD_NOT_ALLOWED:
            case HttpConstants.SC_INTERNAL_SERVER_ERROR:
            case HttpConstants.SC_NOT_FOUND:
            case HttpConstants.SC_FORBIDDEN:
                resultMessage = ServerConnect
                        .getResultInfo(HttpConstants.STATUS_SYSTEM_ERROR);
                break;
            default:
                resultMessage = ServerConnect.getResultInfo(HttpConstants.STATUS_NETWORK_ERROR);
                break;
        }
        return resultMessage;
    }

}
