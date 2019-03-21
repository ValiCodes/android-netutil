package com.yimi.netutil.demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.util.Log;

import com.yimi.netutil.NetInfoUtils;

/**
 * test output:
 * <pre>
 * 03-21 11:17:10.971 D/NetUtil ( 3190): NetUtil Version: 1.0.8 (108)
 * 03-21 11:17:11.104 W/NetUtil-Test( 3190):  [network change] type:WIFI(1) subType:(0) class:UNKNOWN extra:"ymfd-tech"
 * 03-21 11:17:17.899 W/NetUtil-Test( 3190):  [network change] disconnect
 * 03-21 11:17:19.469 W/NetUtil-Test( 3190):  [network change] type:MOBILE(0) subType:LTE(13) class:4G extra:3gnet
 * 03-21 11:17:36.497 W/NetUtil-Test( 3190):  [network change] disconnect
 * 03-21 11:17:58.103 W/NetUtil-Test( 3190):  [network change] type:WIFI(1) subType:(0) class:UNKNOWN extra:"ymfd-tech"
 * 03-21 11:18:06.660 W/NetUtil-Test( 3190):  [network change] disconnect
 * 03-21 11:18:08.017 W/NetUtil-Test( 3190):  [network change] type:MOBILE(0) subType:LTE(13) class:4G extra:3gnet
 * 03-21 11:18:28.460 W/NetUtil-Test( 3190):  [network change] type:WIFI(1) subType:(0) class:UNKNOWN extra:"ymfd-tech"
 * 03-21 11:18:28.496 W/NetUtil-Test( 3190):  [network change] type:WIFI(1) subType:(0) class:UNKNOWN extra:"ymfd-tech"
 * 03-21 11:18:54.131 W/NetUtil-Test( 3190):  [network change] disconnect
 * 03-21 11:18:54.513 W/NetUtil-Test( 3190):  [network change] type:MOBILE(0) subType:LTE(13) class:4G extra:3gnet
 * 03-21 11:19:01.272 W/NetUtil-Test( 3190):  [network change] type:WIFI(1) subType:(0) class:UNKNOWN extra:"ymfd-tech"
 * 03-21 11:19:01.318 W/NetUtil-Test( 3190):  [network change] type:WIFI(1) subType:(0) class:UNKNOWN extra:"ymfd-tech"
 * 03-21 11:19:03.236 W/NetUtil-Test( 3190):  [network change] disconnect
 * 03-21 11:19:03.555 W/NetUtil-Test( 3190):  [network change] type:MOBILE(0) subType:LTE(13) class:4G extra:3gnet
 * 03-21 11:19:05.813 W/NetUtil-Test( 3190):  [network change] disconnect
 * 03-21 11:19:10.133 W/NetUtil-Test( 3190):  [network change] type:WIFI(1) subType:(0) class:UNKNOWN extra:"ymfd-tech"
 * </per>
 */
public class NetInfoUtilsTest {

    private static final String TAG = "NetUtil-Test";

    private static BroadcastReceiver mReceiver;

    public static void unregisterReceiver(@NonNull Context context) {
        if (mReceiver != null) {
            context.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    public static void registerReceiver(@NonNull Context context) {
        if (mReceiver == null) {
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                        ConnectivityManager connectivityManager = (ConnectivityManager)
                                context.getSystemService(Context.CONNECTIVITY_SERVICE);
                        if (connectivityManager == null) {
                            return;
                        }
                        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();

                        // log
                        String netMsg = NetInfoUtils.getNetInfoMsg(netInfo);
                        Log.w(TAG, netMsg);
                    }
                }
            };
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(mReceiver, filter);
    }

}
