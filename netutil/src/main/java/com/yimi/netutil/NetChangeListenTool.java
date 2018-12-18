package com.yimi.netutil;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * Created by liuziwen on 2017/3/27.
 */

public class NetChangeListenTool {
    public static final String TAG = "NetChangeListenTool";
    private static ConnectivityManager mConnectivityManager;// 监听网络
    private static NetChangeReceiver netChangeReceiver;
    private static QiyiNetworkInfo mNetworkInfo;
    private static QiyiNetworkInfo qiyiNetworkInfo; // 上一次网络状态


    @SuppressLint("NewApi")
    public static void registerBroadcastReceiver(Context mContext) {
        mConnectivityManager = (ConnectivityManager) mContext
                .getSystemService(CONNECTIVITY_SERVICE);
        mNetworkInfo = new QiyiNetworkInfo();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0以上版本
            NetworkRequest networkRequest = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
            ConnectivityManager.NetworkCallback networkCallback
                    = new ConnectivityManager.NetworkCallback() {

                @Override
                public void onAvailable(Network network) {
                    // 网络变化
                    Glog.d(TAG, "!----- 高版本-- 网络切换" + network.toString());
                    postNetStatus(mConnectivityManager.getActiveNetworkInfo());
                }

                @Override
                public void onLost(Network network) {
                    super.onLost(network);
                    Glog.d(TAG, "!----- 高版本 --断网--" + network);
                    NetworkInfo currentNetWorkInfo = mConnectivityManager.getActiveNetworkInfo();
                    if (currentNetWorkInfo != null && currentNetWorkInfo.isAvailable()) {
                        return;
                    }
                    postNetStatus(null);
                }
            };
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) mContext.getSystemService(CONNECTIVITY_SERVICE);
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
        } else {
            IntentFilter filter = new IntentFilter();
            netChangeReceiver = new NetChangeReceiver();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            mContext.registerReceiver(netChangeReceiver, filter);
        }
    }

    public static void unregisterBroadcastReceiver(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP && netChangeReceiver != null) {
            context.unregisterReceiver(netChangeReceiver);
        }
    }

    public static void postNetStatus(NetworkInfo networkInfo) {
        mNetworkInfo.update(networkInfo);
        if (networkInfo != null) {
            if (!mNetworkInfo.equals(qiyiNetworkInfo)) {
                qiyiNetworkInfo = mNetworkInfo;
                if (networkInfo.isAvailable()) {
                    Glog.d(TAG, "!----- 网络切换 -----");
                }
            }
        } else {
            Glog.d(TAG, "!----- 断网 -----");
        }
    }

    public static QiyiNetworkInfo getNetworkInfo() {
        Glog.d(TAG, "-----getNetworkInfo-----");
        return mNetworkInfo;
    }

    /**
     * (Build.VERSION_CODES.LOLLIPOP) 5.0以下的机器 ,用旧的广播监听变化.
     */
    private static class NetChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
                Glog.d(TAG, "!----- 系统广播---网络切换" + "   "
                        + (info == null ? "null" : info.toString()));
                postNetStatus(info);
            }
        }
    }

    public static class QiyiNetworkInfo {

        public NetworkInfo networkInfo;

        public boolean mIsAvailable = false;
        private String mTypeName = "";// wifi,mobile
        private String mNetworkType = "";// wifi，2G,3G,4G

        public QiyiNetworkInfo() {
        }

        public boolean isAvailable() {
            return mIsAvailable;
        }

        public String getTypeName() {
            return mTypeName;
        }

        public String getNetworkType() {
            return mNetworkType;
        }

        public void update(NetworkInfo networkInfo) {
            if (networkInfo != null) {
                mIsAvailable = networkInfo.isAvailable();
                mTypeName = networkInfo.getTypeName() == null ? ""
                        : networkInfo.getTypeName();
                mNetworkType = NetUtils.getNetworkTypeName(networkInfo);
                this.networkInfo = networkInfo;
            } else {
                mIsAvailable = false;
                mTypeName = "";
                mNetworkType = "";
                this.networkInfo = null;
            }

        }

        public boolean equals(QiyiNetworkInfo info) {
            boolean result = false;
            if (info != null) {
                // network,typeName,extraInfo
                result = (mIsAvailable == info.isAvailable())
                        && (mTypeName.equals(info.getTypeName()))
                        && mNetworkType.equals(info.getNetworkType());

            }
            return result;
        }

        @Override
        public String toString() {
            return "network isAvailable:" + mIsAvailable +
                    ", network:" + mTypeName +
                    ",networkType:" + mNetworkType;
        }
    }
}

