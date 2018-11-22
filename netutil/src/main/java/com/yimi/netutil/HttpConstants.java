package com.yimi.netutil;

/**
 * Created by liuniu on 2017/12/15.
 */

public class HttpConstants {
    public static final int STATUS_NETWORK_NOT_AVAILABLE = 10002;    // 当前网络不可用
    public static final int STATUS_NETWORK_TIMEOUT = 10003;            // 网络连接超时
    public static final int STATUS_SYSTEM_ERROR = 10004;            // 系统访问异常
    public static final int STATUS_NETWORK_ERROR = 10005;            // 网络连接异常
    public static final String RESPONSE_CODE_9000 = "90000"; //需要重试的错误码
    public static final int STATUS_DATA_PARSE_ERROR = 10006; //数据解析异常
    public static final int STATUS_CERTIFICATE_ERROR = 10007; //数据证书异常

    public static final int SC_MOVED_TEMPORARILY = 302;
    public static final int SC_METHOD_NOT_ALLOWED = 405;
    public static final int SC_INTERNAL_SERVER_ERROR = 500;
    public static final int SC_NOT_FOUND = 404;
    public static final int SC_FORBIDDEN = 403;
    public static final int RESULT_SUCCESS = 200;

    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_COOKIE = "Cookie";

    public final static String Upload_file = "op_upload_file"; //上传文件
    public final static String ping_back = "op_ping_back"; //
    public final static String game_info_api = "op_game_info";

    public static boolean isSuccess(int resultCode) {
        return RESULT_SUCCESS == resultCode;
    }

}
